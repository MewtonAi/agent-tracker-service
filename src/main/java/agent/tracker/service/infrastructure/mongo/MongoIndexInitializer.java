package agent.tracker.service.infrastructure.mongo;

import agent.tracker.service.infrastructure.mongo.MongoIndexManifest.RequiredMongoIndex;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Requires(property = "task.store", value = "mongo")
public class MongoIndexInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(MongoIndexInitializer.class);

    private final MongoClient mongoClient;
    private final String databaseName;

    public MongoIndexInitializer(
        MongoClient mongoClient,
        @Value("${mongodb.database:agent_tracker}") String databaseName
    ) {
        this.mongoClient = mongoClient;
        this.databaseName = databaseName;
    }

    @EventListener
    void onStartup(ServerStartupEvent ignored) {
        var database = mongoClient.getDatabase(databaseName);
        for (RequiredMongoIndex requiredIndex : MongoIndexManifest.REQUIRED_INDEXES) {
            MongoCollection<Document> collection = database.getCollection(requiredIndex.collection());
            Set<String> existingIndexNames = existingIndexNames(collection);

            IndexOptions options = new IndexOptions().name(requiredIndex.name());
            if (requiredIndex.unique()) {
                options.unique(true);
            }
            if (requiredIndex.expireAfterSeconds() != null) {
                options.expireAfter(requiredIndex.expireAfterSeconds(), TimeUnit.SECONDS);
            }

            collection.createIndex(requiredIndex.keys(), options);
            String outcome = existingIndexNames.contains(requiredIndex.name()) ? "existing" : "created";
            LOG.info(
                "event=mongo_index_state db={} collection={} index={} outcome={} keys={} unique={} ttlSeconds={}",
                databaseName,
                requiredIndex.collection(),
                requiredIndex.name(),
                outcome,
                requiredIndex.keys().toJson(),
                requiredIndex.unique(),
                requiredIndex.expireAfterSeconds()
            );
        }
    }

    private static Set<String> existingIndexNames(MongoCollection<Document> collection) {
        Set<String> names = new HashSet<>();
        for (Document index : collection.listIndexes()) {
            String name = index.getString("name");
            if (name != null && !name.isBlank()) {
                names.add(name);
            }
        }
        return names;
    }
}
