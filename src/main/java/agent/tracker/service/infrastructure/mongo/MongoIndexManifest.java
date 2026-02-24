package agent.tracker.service.infrastructure.mongo;

import java.util.List;
import org.bson.Document;

public final class MongoIndexManifest {

    private MongoIndexManifest() {
    }

    public static final List<RequiredMongoIndex> REQUIRED_INDEXES = List.of(
        new RequiredMongoIndex(
            "tasks",
            "tasks_status_updatedAt_taskId_idx",
            new Document("status", 1).append("updatedAt", -1).append("taskId", -1),
            false,
            null
        ),
        new RequiredMongoIndex(
            "tasks",
            "tasks_updatedAt_taskId_idx",
            new Document("updatedAt", -1).append("taskId", -1),
            false,
            null
        ),
        new RequiredMongoIndex(
            "idempotency_records",
            "idempotency_records_operation_key_uk",
            new Document("operation", 1).append("key", 1),
            true,
            null
        ),
        new RequiredMongoIndex(
            "idempotency_records",
            "idempotency_records_expiresAt_ttl_idx",
            new Document("expiresAt", 1),
            false,
            0L
        )
    );

    public record RequiredMongoIndex(
        String collection,
        String name,
        Document keys,
        boolean unique,
        Long expireAfterSeconds
    ) {
    }
}
