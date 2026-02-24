package agent.tracker.service.domain.contract;

import agent.tracker.service.domain.model.TaskStatus;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record UpdateTaskStatusCommand(
    String taskId,
    TaskStatus status,
    String updatedBy,
    String idempotencyKey
) {
}
