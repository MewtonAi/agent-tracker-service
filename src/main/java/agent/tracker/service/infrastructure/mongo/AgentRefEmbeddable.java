package agent.tracker.service.infrastructure.mongo;

import io.micronaut.serde.annotation.Serdeable;
import java.util.Set;

@Serdeable
public record AgentRefEmbeddable(
    String agentId,
    String displayName,
    Set<String> capabilities
) {
}
