package agent.tracker.service.api;

import agent.tracker.service.domain.exception.ConcurrentModificationException;
import agent.tracker.service.domain.exception.IdempotencyKeyReuseMismatchException;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void shouldMapConcurrentModificationToStableConflictCode() {
        HttpRequest<?> request = HttpRequest.GET("/v1/tasks/task-1").header("X-Correlation-Id", "corr-789");

        ApiExceptionHandler.ApiProblem body = handler.handleConcurrentModification(
            request,
            new ConcurrentModificationException("conflict")
        ).body();

        assertEquals(HttpStatus.CONFLICT.getCode(), body.status());
        assertEquals("CONCURRENT_MODIFICATION", body.code());
        assertEquals("corr-789", body.correlationId());
    }

    @Test
    void shouldMapIdempotencyMismatchToStableConflictCode() {
        HttpRequest<?> request = HttpRequest.POST("/v1/tasks", "{}").header("X-Correlation-Id", "corr-555");

        ApiExceptionHandler.ApiProblem body = handler.handleIdempotencyMismatch(
            request,
            new IdempotencyKeyReuseMismatchException("mismatch")
        ).body();

        assertEquals(HttpStatus.CONFLICT.getCode(), body.status());
        assertEquals("IDEMPOTENCY_KEY_REUSE_MISMATCH", body.code());
        assertEquals("corr-555", body.correlationId());
    }
}
