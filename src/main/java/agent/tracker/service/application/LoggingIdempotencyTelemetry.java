package agent.tracker.service.application;

import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class LoggingIdempotencyTelemetry implements IdempotencyTelemetry {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingIdempotencyTelemetry.class);

    @Override
    public void firstWrite(String operation) {
        LOG.info("event=idempotency.first_write operation={}", operation);
    }

    @Override
    public void replayHit(String operation) {
        LOG.info("event=idempotency.replay_hit operation={}", operation);
    }

    @Override
    public void mismatchReject(String operation) {
        LOG.warn("event=idempotency.mismatch_reject operation={}", operation);
    }
}
