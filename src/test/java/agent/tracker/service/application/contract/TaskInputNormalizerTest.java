package agent.tracker.service.application.contract;

import agent.tracker.service.domain.model.TaskStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskInputNormalizerTest {

    private final TaskInputNormalizer normalizer = new TaskInputNormalizer();

    @Test
    void shouldNormalizeStatusLimitAndCursor() {
        assertEquals(TaskStatus.IN_PROGRESS, normalizer.parseStatusFilter(" in_progress "));
        assertEquals(50, normalizer.normalizeLimit(null));
        assertEquals(12, normalizer.normalizeLimit(12));
        assertNull(normalizer.normalizeCursor(" "));
        assertEquals("o:5", normalizer.normalizeCursor(" o:5 "));
    }

    @Test
    void shouldRejectBadStatusOrLimit() {
        assertThrows(IllegalArgumentException.class, () -> normalizer.parseStatusFilter("nope"));
        assertThrows(IllegalArgumentException.class, () -> normalizer.normalizeLimit(0));
        assertThrows(IllegalArgumentException.class, () -> normalizer.normalizeLimit(201));
    }

    @Test
    void shouldTrimAndRequireTextValues() {
        assertEquals("idem-key", normalizer.requireIdempotencyKey(" idem-key "));
        assertThrows(IllegalArgumentException.class, () -> normalizer.requireIdempotencyKey(" "));
        assertEquals("task-1", normalizer.requireText(" task-1 ", "taskId"));
    }
}
