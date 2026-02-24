package agent.tracker.service.api.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record CreateProjectRequest(
    String name,
    String description,
    String requestedBy
) {
}
