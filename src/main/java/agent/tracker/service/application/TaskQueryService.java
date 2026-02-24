package agent.tracker.service.application;

import agent.tracker.service.domain.exception.NotFoundException;
import agent.tracker.service.domain.model.Task;
import agent.tracker.service.domain.model.TaskStatus;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class TaskQueryService {

    private final TaskStore store;

    public TaskQueryService(TaskStore store) {
        this.store = store;
    }

    public Task getTaskById(String taskId) {
        Task task = store.findTaskById(taskId);
        if (task == null) {
            throw new NotFoundException("Task not found: " + taskId);
        }
        return task;
    }

    public List<Task> listTasks(TaskStatus status) {
        return store.listTasks(status);
    }
}
