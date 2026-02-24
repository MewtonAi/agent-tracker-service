package agent.tracker.service.api.dto;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;

@Serdeable
public record AssignTaskRequest(
    String taskId,
    @NotBlank String agentId,
    @NotBlank String agentDisplayName,
    Set<String> capabilities,
    @NotBlank String requestedBy
) {
}
