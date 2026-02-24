package agent.tracker.service.api;

import agent.tracker.service.domain.exception.InvalidTaskTransitionException;
import agent.tracker.service.domain.exception.NotFoundException;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.Controller;
import java.time.Instant;

@Produces
@Controller
public class ApiExceptionHandler {

    @Error(global = true, exception = NotFoundException.class)
    public HttpResponse<ApiError> handleNotFound(HttpRequest<?> request, NotFoundException exception) {
        return HttpResponse.status(HttpStatus.NOT_FOUND)
            .body(ApiError.of(HttpStatus.NOT_FOUND, exception.getMessage(), request.getPath()));
    }

    @Error(global = true, exception = InvalidTaskTransitionException.class)
    public HttpResponse<ApiError> handleConflict(HttpRequest<?> request, InvalidTaskTransitionException exception) {
        return HttpResponse.status(HttpStatus.CONFLICT)
            .body(ApiError.of(HttpStatus.CONFLICT, exception.getMessage(), request.getPath()));
    }

    @Error(global = true, exception = IllegalArgumentException.class)
    public HttpResponse<ApiError> handleBadRequest(HttpRequest<?> request, IllegalArgumentException exception) {
        return HttpResponse.badRequest(ApiError.of(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getPath()));
    }

    public record ApiError(Instant timestamp, int status, String error, String message, String path) {
        static ApiError of(HttpStatus status, String message, String path) {
            return new ApiError(Instant.now(), status.getCode(), status.getReason(), message, path);
        }
    }
}
