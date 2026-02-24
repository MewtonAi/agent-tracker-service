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
    private final long idempotencyTtlHours;

    public MongoIndexInitializer(MongoClient mongoClient, @Value("${idempotency.ttl-hours:72}") long idempotencyTtlHours) {
        this.mongoClient = mongoClient;
        this.idempotencyTtlHours = idempotencyTtlHours;
    }

    @EventListener
    void onStartup(ServerStartupEvent ignored) {
        MongoCollection<Document> tasks = mongoClient.getDatabase("agent_tracker").getCollection("tasks");
        tasks.createIndex(Indexes.compoundIndex(Indexes.ascending("status"), Indexes.descending("updatedAt")));
        tasks.createIndex(Indexes.descending("updatedAt"));

        MongoCollection<Document> idem = mongoClient.getDatabase("agent_tracker").getCollection("idempotency_records");
        idem.createIndex(Indexes.ascending("key"), new IndexOptions().unique(true));
        idem.createIndex(
            Indexes.ascending("createdAt"),
            new IndexOptions().expireAfter(idempotencyTtlHours * 3600, java.util.concurrent.TimeUnit.SECONDS)
        );
    }
}
