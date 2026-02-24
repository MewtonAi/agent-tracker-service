package agent.tracker.service.application;

import agent.tracker.service.domain.model.Task;
import agent.tracker.service.domain.model.TaskStatus;
import java.util.List;

public interface TaskStore {
    Task findTaskById(String taskId);

    List<Task> listTasks(TaskStatus status);

    Task findCreateReplay(String idempotencyKey);

    Task findStatusReplay(String idempotencyKey);

    void saveCreateReplay(String idempotencyKey, Task task);

    void saveStatusReplay(String idempotencyKey, Task task);

    Task save(Task task);
}
