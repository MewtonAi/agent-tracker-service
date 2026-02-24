package agent.tracker.service.api;

import agent.tracker.service.domain.exception.ConcurrentModificationException;
import agent.tracker.service.domain.exception.ConflictException;
import agent.tracker.service.domain.exception.IdempotencyKeyReuseMismatchException;
import agent.tracker.service.domain.exception.InvalidTaskTransitionException;
import agent.tracker.service.domain.exception.NotFoundException;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.exceptions.HttpStatusException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Locks external REST error code contract per ADR-003/004/006.
 */
class ErrorCatalogContractTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();
    private final HttpRequest<?> request = HttpRequest.GET("/v1/tasks/task-1");

    @Test
    void shouldKeepStableCodeForNotFound() {
        assertEquals("TASK_NOT_FOUND", handler.handleNotFound(request, new NotFoundException("missing")).body().code());
    }

    @Test
    void shouldKeepStableCodeForInvalidTransition() {
        assertEquals("INVALID_TASK_TRANSITION", handler.handleConflict(request, new InvalidTaskTransitionException("bad")).body().code());
    }

    @Test
    void shouldKeepStableCodeForConcurrentModification() {
        assertEquals("CONCURRENT_MODIFICATION", handler.handleConcurrentModification(request, new ConcurrentModificationException("conflict")).body().code());
    }

    @Test
    void shouldKeepStableCodeForIdempotencyMismatch() {
        assertEquals("IDEMPOTENCY_KEY_REUSE_MISMATCH", handler.handleIdempotencyMismatch(request, new IdempotencyKeyReuseMismatchException("mismatch")).body().code());
    }

    @Test
    void shouldKeepStableCodeForConflict() {
        assertEquals("TASK_CONFLICT", handler.handleConflict(request, new ConflictException("conflict")).body().code());
    }

    @Test
    void shouldKeepStableCodeForBadRequest() {
        assertEquals("BAD_REQUEST", handler.handleBadRequest(request, new IllegalArgumentException("bad")).body().code());
    }

    @Test
    void shouldKeepHttpStatusCodeConvention() {
        ApiExceptionHandler.ApiProblem body = handler.handleHttpStatus(
            request,
            new HttpStatusException(io.micronaut.http.HttpStatus.SERVICE_UNAVAILABLE, "down")
        ).body();
        assertEquals("HTTP_503", body.code());
    }
}
