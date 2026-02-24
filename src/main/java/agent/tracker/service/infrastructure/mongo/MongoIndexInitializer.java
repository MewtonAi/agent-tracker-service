package agent.tracker.service.infrastructure.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;
import org.bson.Document;

@Singleton
@Requires(property = "task.store", value = "mongo")
public class MongoIndexInitializer {

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
        MongoCollection<Document> tasks = mongoClient.getDatabase(databaseName).getCollection("tasks");
        tasks.createIndex(Indexes.compoundIndex(Indexes.ascending("status"), Indexes.descending("updatedAt"), Indexes.descending("taskId")));
        tasks.createIndex(Indexes.compoundIndex(Indexes.descending("updatedAt"), Indexes.descending("taskId")));

        MongoCollection<Document> idem = mongoClient.getDatabase(databaseName).getCollection("idempotency_records");
        idem.createIndex(
            Indexes.compoundIndex(Indexes.ascending("operation"), Indexes.ascending("key")),
            new IndexOptions().unique(true)
        );
        idem.createIndex(Indexes.ascending("expiresAt"), new IndexOptions().expireAfter(0L, java.util.concurrent.TimeUnit.SECONDS));
    }
}
