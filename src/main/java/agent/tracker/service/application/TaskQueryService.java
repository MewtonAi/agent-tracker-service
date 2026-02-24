package agent.tracker.service.application;

import agent.tracker.service.domain.exception.NotFoundException;
import agent.tracker.service.domain.model.Task;
import agent.tracker.service.domain.model.TaskStatus;
import jakarta.inject.Singleton;
import java.util.Comparator;
import java.util.List;

@Singleton
public class TaskQueryService {

    private final InMemoryTaskStore store;

    public TaskQueryService(InMemoryTaskStore store) {
        this.store = store;
    }

    public Task getTaskById(String taskId) {
        Task task = store.tasks().get(taskId);
        if (task == null) {
            throw new NotFoundException("Task not found: " + taskId);
        }
        return task;
    }

    public List<Task> listTasks(TaskStatus status) {
        return store.tasks().values().stream()
            .filter(task -> status == null || status == task.getStatus())
            .sorted(Comparator.comparing((Task t) -> t.getAudit().getUpdatedAt()).reversed())
            .toList();
    }
}
