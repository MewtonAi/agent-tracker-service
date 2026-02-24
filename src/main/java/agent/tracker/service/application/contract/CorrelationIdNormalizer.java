package agent.tracker.service.application.contract;

import jakarta.inject.Singleton;
import java.util.UUID;

@Singleton
public class CorrelationIdNormalizer {

    public String normalizeOrGenerate(String requestedCorrelationId) {
        if (requestedCorrelationId == null || requestedCorrelationId.isBlank()) {
            return UUID.randomUUID().toString();
        }

        try {
            return UUID.fromString(requestedCorrelationId.trim()).toString();
        } catch (IllegalArgumentException ignored) {
            return UUID.randomUUID().toString();
        }
    }
}
