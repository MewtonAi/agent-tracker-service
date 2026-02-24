package agent.tracker.service.domain.exception;

public class ConcurrentModificationException extends ConflictException {
    public ConcurrentModificationException(String message) {
        super(message);
    }
}
