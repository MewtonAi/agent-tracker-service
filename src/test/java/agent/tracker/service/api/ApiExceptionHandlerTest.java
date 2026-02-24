package agent.tracker.service.api;

import agent.tracker.service.application.contract.CorrelationIdNormalizer;
import agent.tracker.service.domain.exception.ConcurrentModificationException;
import agent.tracker.service.domain.exception.IdempotencyKeyReuseMismatchException;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler(new CorrelationIdNormalizer());

    @Test
    void shouldMapConcurrentModificationToStableConflictCode() {
        String requestedCorrelationId = "123e4567-e89b-12d3-a456-426614174000";
        HttpRequest<?> request = HttpRequest.GET("/v1/tasks/task-1").header("X-Correlation-Id", requestedCorrelationId);

        ApiExceptionHandler.ApiProblem body = handler.handleConcurrentModification(
            request,
            new ConcurrentModificationException("conflict")
        ).body();

        assertEquals(HttpStatus.CONFLICT.getCode(), body.status());
        assertEquals("CONCURRENT_MODIFICATION", body.code());
        assertEquals(requestedCorrelationId, body.correlationId());
    }

    @Test
    void shouldGenerateCorrelationIdWhenHeaderIsMalformed() {
        HttpRequest<?> request = HttpRequest.POST("/v1/tasks", "{}").header("X-Correlation-Id", "corr-555");

        ApiExceptionHandler.ApiProblem body = handler.handleIdempotencyMismatch(
            request,
            new IdempotencyKeyReuseMismatchException("mismatch")
        ).body();

        assertEquals(HttpStatus.CONFLICT.getCode(), body.status());
        assertEquals("IDEMPOTENCY_KEY_REUSE_MISMATCH", body.code());
        assertDoesNotThrow(() -> UUID.fromString(body.correlationId()));
    }
}
