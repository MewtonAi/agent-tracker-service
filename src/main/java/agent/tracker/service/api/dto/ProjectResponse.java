package agent.tracker.service.api.dto;

import agent.tracker.service.domain.model.AuditMetadata;
import agent.tracker.service.domain.model.ProjectStatus;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record ProjectResponse(
    String projectId,
    String name,
    String description,
    ProjectStatus status,
    AuditMetadata audit
) {
}
