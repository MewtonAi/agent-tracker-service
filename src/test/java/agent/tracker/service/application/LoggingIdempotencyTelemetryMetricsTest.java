package agent.tracker.service.application;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoggingIdempotencyTelemetryMetricsTest {

    @Test
    void shouldIncrementCountersByEventAndOperation() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        LoggingIdempotencyTelemetry telemetry = new LoggingIdempotencyTelemetry(meterRegistry);

        telemetry.firstWrite("create_task");
        telemetry.replayHit("create_task");
        telemetry.mismatchReject("update_task_status");
        telemetry.replayHit("create_task");

        assertEquals(1.0, meterRegistry.counter("agent_tracker_idempotency_events_total", "event", "first_write", "operation", "create_task").count());
        assertEquals(2.0, meterRegistry.counter("agent_tracker_idempotency_events_total", "event", "replay_hit", "operation", "create_task").count());
        assertEquals(1.0, meterRegistry.counter("agent_tracker_idempotency_events_total", "event", "mismatch_reject", "operation", "update_task_status").count());
    }
}
