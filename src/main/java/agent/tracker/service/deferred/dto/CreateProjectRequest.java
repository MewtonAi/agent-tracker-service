package agent.tracker.service.deferred.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record CreateProjectRequest(
    String name,
    String description,
    String requestedBy
) {
}
