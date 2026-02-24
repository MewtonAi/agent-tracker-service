package agent.tracker.service.domain.model;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;
import lombok.Value;
import lombok.With;

@Value
@With
@Builder(toBuilder = true)
@Serdeable
public class Task {
    String taskId;
    String title;
    String description;
    TaskType taskType;
    TaskStatus status;
    TaskPriority priority;
    AgentRef assignee;
    AuditMetadata audit;
}
