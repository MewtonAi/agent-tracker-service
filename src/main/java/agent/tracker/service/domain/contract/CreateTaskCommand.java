package agent.tracker.service.domain.contract;

import agent.tracker.service.domain.model.TaskPriority;
import agent.tracker.service.domain.model.TaskType;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record CreateTaskCommand(
    String projectId,
    String title,
    String description,
    TaskType taskType,
    TaskPriority priority,
    String createdBy
) {
}
