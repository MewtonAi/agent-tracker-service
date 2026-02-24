package agent.tracker.service.domain.model;

import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;
import lombok.With;

@Value
@With
@Builder(toBuilder = true)
@Serdeable
public class AuditMetadata {
    Instant createdAt;
    String createdBy;
    Instant updatedAt;
    String updatedBy;
}
