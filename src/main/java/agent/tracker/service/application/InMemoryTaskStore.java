package agent.tracker.service.application;

import agent.tracker.service.domain.exception.IdempotencyKeyReuseMismatchException;
import agent.tracker.service.domain.model.Task;
import agent.tracker.service.domain.model.TaskStatus;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@Requires(missingProperty = "task.store")
public class InMemoryTaskStore implements TaskStore {
    private final Map<String, Task> tasks = new ConcurrentHashMap<>();
    private final Map<String, ReplayRecord> createByIdempotency = new ConcurrentHashMap<>();
    private final Map<String, ReplayRecord> statusByIdempotency = new ConcurrentHashMap<>();

    @Override
    public Task findTaskById(String taskId) {
        return tasks.get(taskId);
    }

    @Override
    public List<Task> listTasks(TaskStatus status) {
        return sortedTasks(status);
    }

    @Override
    public TaskStorePage listTasksPage(TaskStatus status, int offset, int limit) {
        List<Task> sorted = sortedTasks(status);
        if (offset >= sorted.size()) {
            return new TaskStorePage(List.of(), false);
        }

        int toIndex = Math.min(offset + limit, sorted.size());
        return new TaskStorePage(sorted.subList(offset, toIndex), toIndex < sorted.size());
    }

    @Override
    public Task findCreateReplay(String idempotencyKey, String payloadHash) {
        ReplayRecord record = createByIdempotency.get(idempotencyKey);
        return resolveReplay(idempotencyKey, payloadHash, record);
    }

    @Override
    public Task findStatusReplay(String taskId, String idempotencyKey, String payloadHash) {
        ReplayRecord record = statusByIdempotency.get(statusScope(taskId, idempotencyKey));
        return resolveReplay(idempotencyKey, payloadHash, record);
    }

    @Override
    public void saveCreateReplay(String idempotencyKey, String payloadHash, Task task) {
        createByIdempotency.put(idempotencyKey, new ReplayRecord(payloadHash, task));
    }

    @Override
    public void saveStatusReplay(String taskId, String idempotencyKey, String payloadHash, Task task) {
        statusByIdempotency.put(statusScope(taskId, idempotencyKey), new ReplayRecord(payloadHash, task));
    }

    @Override
    public Task save(Task task) {
        tasks.put(task.getTaskId(), task);
        return task;
    }

    private List<Task> sortedTasks(TaskStatus status) {
        return tasks.values().stream()
            .filter(task -> status == null || status == task.getStatus())
            .sorted(Comparator.comparing((Task t) -> t.getAudit().getUpdatedAt()).reversed().thenComparing(Task::getTaskId, Comparator.reverseOrder()))
            .toList();
    }

    private static String statusScope(String taskId, String idempotencyKey) {
        return taskId + ":" + idempotencyKey;
    }

    private static Task resolveReplay(String idempotencyKey, String payloadHash, ReplayRecord record) {
        if (record == null) {
            return null;
        }
        if (!record.payloadHash().equals(payloadHash)) {
            throw new IdempotencyKeyReuseMismatchException("Idempotency key was already used with a different payload: " + idempotencyKey);
        }
        return record.task();
    }

    private record ReplayRecord(String payloadHash, Task task) {
    }
}
