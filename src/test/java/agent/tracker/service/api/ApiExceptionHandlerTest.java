package agent.tracker.service.api;

import agent.tracker.service.domain.exception.ConcurrentModificationException;
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
}
