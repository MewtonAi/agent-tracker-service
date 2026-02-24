package agent.tracker.service.application;

import agent.tracker.service.domain.model.AuditMetadata;
import agent.tracker.service.domain.model.Task;
import agent.tracker.service.domain.model.TaskPriority;
import agent.tracker.service.domain.model.TaskStatus;
import agent.tracker.service.domain.model.TaskType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskQueryServiceTest {

    @Test
    void shouldPageAndEmitNextCursorUntilEndOfStream() {
        TaskQueryService service = new TaskQueryService(new StubTaskStore(List.of(task("t-1"), task("t-2"), task("t-3"))));

        TaskListPage first = service.listTasks(null, null, 2);
        assertEquals(List.of("t-3", "t-2"), first.tasks().stream().map(Task::getTaskId).toList());
        assertEquals("2", first.nextCursor());

        TaskListPage second = service.listTasks(null, first.nextCursor(), 2);
        assertEquals(List.of("t-1"), second.tasks().stream().map(Task::getTaskId).toList());
        assertNull(second.nextCursor());
    }

    @Test
    void shouldDelegatePaginationWindowToStore() {
        StubTaskStore store = new StubTaskStore(List.of(task("t-1"), task("t-2"), task("t-3")));
        TaskQueryService service = new TaskQueryService(store);

        service.listTasks(null, "1", 2);

        assertEquals(1, store.lastOffset);
        assertEquals(2, store.lastLimit);
        assertEquals(1, store.listPageCalls);
        assertEquals(0, store.fullListCalls);
    }

    @Test
    void shouldAcceptOffsetCursorTokenPrefix() {
        StubTaskStore store = new StubTaskStore(List.of(task("t-1"), task("t-2"), task("t-3")));
        TaskQueryService service = new TaskQueryService(store);

        service.listTasks(null, "o:2", 1);
        assertEquals(2, store.lastOffset);
        assertEquals(1, store.lastLimit);

        service.listTasks(null, "O: 1", 1);
        assertEquals(1, store.lastOffset);
        assertEquals(1, store.lastLimit);
    }

    @Test
    void shouldRejectInvalidCursorAndLimitBounds() {
        TaskQueryService service = new TaskQueryService(new StubTaskStore(List.of(task("t-1"))));

        assertThrows(IllegalArgumentException.class, () -> service.listTasks(null, "-1", 10));
        assertThrows(IllegalArgumentException.class, () -> service.listTasks(null, "abc", 10));
        assertThrows(IllegalArgumentException.class, () -> service.listTasks(null, "o:-1", 10));
        assertThrows(IllegalArgumentException.class, () -> service.listTasks(null, "o:abc", 10));
        assertThrows(IllegalArgumentException.class, () -> service.listTasks(null, "o:", 10));
        assertThrows(IllegalArgumentException.class, () -> service.listTasks(null, "s:1", 10));
        assertThrows(IllegalArgumentException.class, () -> service.listTasks(null, null, 0));
        assertThrows(IllegalArgumentException.class, () -> service.listTasks(null, null, 201));
    }

    @Test
    void shouldReturnEmptyPageForCursorPastEnd() {
        TaskQueryService service = new TaskQueryService(new StubTaskStore(List.of(task("t-1"), task("t-2"))));

        TaskListPage page = service.listTasks(null, "10", 10);
        assertEquals(0, page.tasks().size());
        assertNull(page.nextCursor());
    }

    private static Task task(String taskId) {
        Instant fixed = Instant.parse("2026-01-01T00:00:00Z");
        return Task.builder()
            .taskId(taskId)
            .version(1L)
            .title(taskId)
            .description("desc")
            .taskType(TaskType.FEATURE)
            .status(TaskStatus.OPEN)
            .priority(TaskPriority.MEDIUM)
            .audit(AuditMetadata.builder()
                .createdAt(fixed)
                .createdBy("tester")
                .updatedAt(fixed)
                .updatedBy("tester")
                .build())
            .build();
    }

    private static final class StubTaskStore implements TaskStore {
        private final List<Task> tasks;
        private int fullListCalls;
        private int listPageCalls;
        private int lastOffset = -1;
        private int lastLimit = -1;

        private StubTaskStore(List<Task> tasks) {
            this.tasks = new ArrayList<>(tasks);
        }

        @Override
        public Task findTaskById(String taskId) {
            return tasks.stream().filter(task -> task.getTaskId().equals(taskId)).findFirst().orElse(null);
        }

        @Override
        public List<Task> listTasks(TaskStatus status) {
            fullListCalls++;
            return ordered(status);
        }

        @Override
        public TaskStorePage listTasksPage(TaskStatus status, int offset, int limit) {
            listPageCalls++;
            lastOffset = offset;
            lastLimit = limit;

            List<Task> ordered = ordered(status);
            if (offset >= ordered.size()) {
                return new TaskStorePage(List.of(), false);
            }
            int toIndex = Math.min(offset + limit, ordered.size());
            return new TaskStorePage(ordered.subList(offset, toIndex), toIndex < ordered.size());
        }

        @Override
        public Task findCreateReplay(String idempotencyKey, String payloadHash) {
            return null;
        }

        @Override
        public Task findStatusReplay(String taskId, String idempotencyKey, String payloadHash) {
            return null;
        }

        @Override
        public void saveCreateReplay(String idempotencyKey, String payloadHash, Task task) {
        }

        @Override
        public void saveStatusReplay(String taskId, String idempotencyKey, String payloadHash, Task task) {
        }

        @Override
        public Task save(Task task) {
            tasks.add(task);
            return task;
        }

        private List<Task> ordered(TaskStatus status) {
            return tasks.stream()
                .filter(task -> status == null || task.getStatus() == status)
                .sorted(Comparator.comparing((Task task) -> task.getAudit().getUpdatedAt()).reversed().thenComparing(Task::getTaskId, Comparator.reverseOrder()))
                .toList();
        }
    }
}
