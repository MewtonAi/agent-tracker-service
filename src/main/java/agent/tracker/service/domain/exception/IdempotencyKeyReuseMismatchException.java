package agent.tracker.service.domain.exception;

public class IdempotencyKeyReuseMismatchException extends ConflictException {
    public IdempotencyKeyReuseMismatchException(String message) {
        super(message);
    }
}
