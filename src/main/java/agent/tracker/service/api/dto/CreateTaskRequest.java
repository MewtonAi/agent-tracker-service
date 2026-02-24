package agent.tracker.service.api.dto;

import agent.tracker.service.domain.model.TaskPriority;
import agent.tracker.service.domain.model.TaskType;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;

@Serdeable
public record CreateTaskRequest(
    @NotBlank String title,
    String description,
    TaskType taskType,
    TaskPriority priority,
    @NotBlank String requestedBy
) {
}
