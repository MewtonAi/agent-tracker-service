package agent.tracker.service.domain.contract;

import agent.tracker.service.domain.model.AgentRef;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record AssignTaskCommand(
    String taskId,
    AgentRef assignee,
    String updatedBy
) {
}
