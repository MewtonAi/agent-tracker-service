package agent.tracker.service.infrastructure.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import io.micronaut.context.annotation.Requires;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;
import org.bson.Document;

@Singleton
@Requires(property = "task.store", value = "mongo")
public class MongoIndexInitializer {

    private final MongoClient mongoClient;

    public MongoIndexInitializer(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    @EventListener
    void onStartup(ServerStartupEvent ignored) {
        MongoCollection<Document> tasks = mongoClient.getDatabase("agent_tracker").getCollection("tasks");
        tasks.createIndex(Indexes.compoundIndex(Indexes.ascending("status"), Indexes.descending("updatedAt")));
        tasks.createIndex(Indexes.compoundIndex(Indexes.ascending("priority"), Indexes.descending("updatedAt")));

        MongoCollection<Document> idem = mongoClient.getDatabase("agent_tracker").getCollection("idempotency_records");
        idem.createIndex(Indexes.ascending("key"), new IndexOptions().unique(true));
    }
}
