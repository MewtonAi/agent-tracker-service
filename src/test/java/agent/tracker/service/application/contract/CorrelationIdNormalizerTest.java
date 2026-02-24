package agent.tracker.service.application.contract;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CorrelationIdNormalizerTest {

    private final CorrelationIdNormalizer normalizer = new CorrelationIdNormalizer();

    @Test
    void shouldKeepValidUuidCorrelationId() {
        String correlationId = "123e4567-e89b-12d3-a456-426614174099";
        assertEquals(correlationId, normalizer.normalizeOrGenerate(correlationId));
    }

    @Test
    void shouldGenerateUuidWhenCorrelationIdIsInvalid() {
        String normalized = normalizer.normalizeOrGenerate("not-a-uuid");
        assertDoesNotThrow(() -> UUID.fromString(normalized));
        assertNotEquals("not-a-uuid", normalized);
    }
}
