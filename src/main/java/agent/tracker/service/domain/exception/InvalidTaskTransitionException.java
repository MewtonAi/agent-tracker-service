package agent.tracker.service.domain.exception;

public class InvalidTaskTransitionException extends RuntimeException {
    public InvalidTaskTransitionException(String message) {
        super(message);
    }
}
