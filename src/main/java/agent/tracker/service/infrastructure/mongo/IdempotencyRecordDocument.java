package agent.tracker.service.infrastructure.mongo;

import io.micronaut.data.annotation.DateCreated;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;

@Serdeable
@MappedEntity("idempotency_records")
public record IdempotencyRecordDocument(
    @Id String key,
    String taskId,
    @DateCreated Instant createdAt
) {
}
