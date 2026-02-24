package agent.tracker.service.api.dto;

import agent.tracker.service.domain.model.TaskStatus;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Serdeable
public record UpdateTaskStatusRequest(
    @NotNull TaskStatus status,
    @NotBlank String requestedBy
) {
}
