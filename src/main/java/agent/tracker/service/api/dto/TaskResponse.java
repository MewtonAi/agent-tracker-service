package agent.tracker.service.api.dto;

import agent.tracker.service.domain.model.AgentRef;
import agent.tracker.service.domain.model.AuditMetadata;
import agent.tracker.service.domain.model.TaskPriority;
import agent.tracker.service.domain.model.TaskStatus;
import agent.tracker.service.domain.model.TaskType;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record TaskResponse(
    String taskId,
    String projectId,
    String title,
    String description,
    TaskType taskType,
    TaskStatus status,
    TaskPriority priority,
    AgentRef assignee,
    AuditMetadata audit
) {
}
