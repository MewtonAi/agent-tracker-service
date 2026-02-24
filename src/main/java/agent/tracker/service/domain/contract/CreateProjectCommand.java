package agent.tracker.service.domain.contract;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record CreateProjectCommand(
    String name,
    String description,
    String createdBy
) {
}
