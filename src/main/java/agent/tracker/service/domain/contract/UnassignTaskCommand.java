package agent.tracker.service.domain.contract;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record UnassignTaskCommand(
    String taskId,
    String updatedBy
) {
}
