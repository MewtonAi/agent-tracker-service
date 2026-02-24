package agent.tracker.service.api;

import agent.tracker.service.application.contract.CorrelationIdNormalizer;
import agent.tracker.service.domain.exception.ConcurrentModificationException;
import agent.tracker.service.domain.exception.ConflictException;
import agent.tracker.service.domain.exception.IdempotencyKeyReuseMismatchException;
import agent.tracker.service.domain.exception.InvalidTaskTransitionException;
import agent.tracker.service.domain.exception.NotFoundException;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.exceptions.HttpStatusException;
import jakarta.validation.ConstraintViolationException;

@Produces
@Controller
public class ApiExceptionHandler {

    private final CorrelationIdNormalizer correlationIdNormalizer;

    public ApiExceptionHandler(CorrelationIdNormalizer correlationIdNormalizer) {
        this.correlationIdNormalizer = correlationIdNormalizer;
    }

    @Error(global = true, exception = NotFoundException.class)
    public HttpResponse<ApiProblem> handleNotFound(HttpRequest<?> request, NotFoundException exception) {
        return respond(request, HttpStatus.NOT_FOUND, "TASK_NOT_FOUND", exception.getMessage());
    }

    @Error(global = true, exception = InvalidTaskTransitionException.class)
    public HttpResponse<ApiProblem> handleConflict(HttpRequest<?> request, InvalidTaskTransitionException exception) {
        return respond(request, HttpStatus.CONFLICT, "INVALID_TASK_TRANSITION", exception.getMessage());
    }

    @Error(global = true, exception = ConcurrentModificationException.class)
    public HttpResponse<ApiProblem> handleConcurrentModification(HttpRequest<?> request, ConcurrentModificationException exception) {
        return respond(request, HttpStatus.CONFLICT, "CONCURRENT_MODIFICATION", exception.getMessage());
    }

    @Error(global = true, exception = IdempotencyKeyReuseMismatchException.class)
    public HttpResponse<ApiProblem> handleIdempotencyMismatch(HttpRequest<?> request, IdempotencyKeyReuseMismatchException exception) {
        return respond(request, HttpStatus.CONFLICT, "IDEMPOTENCY_KEY_REUSE_MISMATCH", exception.getMessage());
    }

    @Error(global = true, exception = ConflictException.class)
    public HttpResponse<ApiProblem> handleConflict(HttpRequest<?> request, ConflictException exception) {
        return respond(request, HttpStatus.CONFLICT, "TASK_CONFLICT", exception.getMessage());
    }

    @Error(global = true, exception = ConstraintViolationException.class)
    public HttpResponse<ApiProblem> handleValidation(HttpRequest<?> request, ConstraintViolationException exception) {
        return respond(request, HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", exception.getMessage());
    }

    @Error(global = true, exception = IllegalArgumentException.class)
    public HttpResponse<ApiProblem> handleBadRequest(HttpRequest<?> request, IllegalArgumentException exception) {
        return respond(request, HttpStatus.BAD_REQUEST, "BAD_REQUEST", exception.getMessage());
    }

    @Error(global = true, exception = HttpStatusException.class)
    public HttpResponse<ApiProblem> handleHttpStatus(HttpRequest<?> request, HttpStatusException exception) {
        HttpStatus status = exception.getStatus();
        return respond(request, status, "HTTP_" + status.getCode(), exception.getMessage());
    }

    @Error(global = true, exception = Exception.class)
    public HttpResponse<ApiProblem> handleUnknown(HttpRequest<?> request, Exception exception) {
        return respond(request, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred");
    }

    private HttpResponse<ApiProblem> respond(HttpRequest<?> request, HttpStatus status, String code, String detail) {
        String correlationId = correlationIdNormalizer.normalizeOrGenerate(request.getHeaders().get("X-Correlation-Id"));
        ApiProblem body = new ApiProblem(
            "https://api.agent-tracker/errors/" + code.toLowerCase(),
            status.getReason(),
            status.getCode(),
            detail,
            request.getPath(),
            code,
            correlationId
        );
        return HttpResponse.status(status)
            .header("X-Correlation-Id", correlationId)
            .body(body);
    }

    public record ApiProblem(
        String type,
        String title,
        int status,
        String detail,
        String instance,
        String code,
        String correlationId
    ) {
    }
}
