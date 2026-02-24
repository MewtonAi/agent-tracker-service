package agent.tracker.service.domain.model;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;
import lombok.Value;
import lombok.With;

@Value
@With
@Builder(toBuilder = true)
@Serdeable
public class Project {
    String projectId;
    String name;
    String description;
    ProjectStatus status;
    AuditMetadata audit;
}
