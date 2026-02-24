package agent.tracker.service.application;

import agent.tracker.service.domain.model.Task;
import jakarta.inject.Singleton;
import agent.tracker.service.domain.model.TaskStatus;
import io.micronaut.context.annotation.Requires;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@Requires(missingProperty = "task.store")
public class InMemoryTaskStore implements TaskStore {
    private final Map<String, Task> tasks = new ConcurrentHashMap<>();
    private final Map<String, Task> createByIdempotency = new ConcurrentHashMap<>();
    private final Map<String, Task> statusByIdempotency = new ConcurrentHashMap<>();

    @Override
    public Task findTaskById(String taskId) {
        return tasks.get(taskId);
    }

    @Override
    public List<Task> listTasks(TaskStatus status) {
        return tasks.values().stream()
            .filter(task -> status == null || status == task.getStatus())
            .sorted(Comparator.comparing((Task t) -> t.getAudit().getUpdatedAt()).reversed())
            .toList();
    }

    @Override
    public Task findCreateReplay(String idempotencyKey) {
        return createByIdempotency.get(idempotencyKey);
    }

    @Override
    public Task findStatusReplay(String idempotencyKey) {
        return statusByIdempotency.get(idempotencyKey);
    }

    @Override
    public void saveCreateReplay(String idempotencyKey, Task task) {
        createByIdempotency.put(idempotencyKey, task);
    }

    @Override
    public void saveStatusReplay(String idempotencyKey, Task task) {
        statusByIdempotency.put(idempotencyKey, task);
    }

    @Override
    public Task save(Task task) {
        tasks.put(task.getTaskId(), task);
        return task;
    }
}
