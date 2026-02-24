package agent.tracker.service.application;

public interface IdempotencyTelemetry {
    void firstWrite(String operation);

    void replayHit(String operation);

    void mismatchReject(String operation);
}
