package agent.tracker.service.domain.contract;

import agent.tracker.service.domain.model.ProjectStatus;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record UpdateProjectCommand(
    String projectId,
    String name,
    String description,
    ProjectStatus status,
    String updatedBy
) {
}
