package agent.tracker.service.application;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class LoggingIdempotencyTelemetry implements IdempotencyTelemetry {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingIdempotencyTelemetry.class);
    private static final String METRIC_NAME = "agent_tracker_idempotency_events_total";

    private final MeterRegistry meterRegistry;

    public LoggingIdempotencyTelemetry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void firstWrite(String operation) {
        LOG.info("event=idempotency.first_write operation={}", operation);
        increment("first_write", operation);
    }

    @Override
    public void replayHit(String operation) {
        LOG.info("event=idempotency.replay_hit operation={}", operation);
        increment("replay_hit", operation);
    }

    @Override
    public void mismatchReject(String operation) {
        LOG.warn("event=idempotency.mismatch_reject operation={}", operation);
        increment("mismatch_reject", operation);
    }

    private void increment(String event, String operation) {
        Counter.builder(METRIC_NAME)
            .description("Idempotency event totals by event type and operation")
            .tag("event", event)
            .tag("operation", operation)
            .register(meterRegistry)
            .increment();
    }
}
