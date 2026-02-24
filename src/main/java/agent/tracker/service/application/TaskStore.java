package agent.tracker.service.application;

import agent.tracker.service.domain.model.Task;
import agent.tracker.service.domain.model.TaskStatus;
import java.util.List;

public interface TaskStore {
    Task findTaskById(String taskId);

    List<Task> listTasks(TaskStatus status);

    Task findCreateReplay(String idempotencyKey, String payloadHash);

    Task findStatusReplay(String taskId, String idempotencyKey, String payloadHash);

    void saveCreateReplay(String idempotencyKey, String payloadHash, Task task);

    void saveStatusReplay(String taskId, String idempotencyKey, String payloadHash, Task task);

    Task save(Task task);
}
