package agent.tracker.service.domain.model;

import io.micronaut.serde.annotation.Serdeable;
import java.util.Set;
import lombok.Builder;

@Builder(toBuilder = true)
@Serdeable
public record AgentRef(
    String agentId,
    String displayName,
    Set<String> capabilities
) {
}
