package agent.tracker.service.application;

import agent.tracker.service.domain.model.Task;
import agent.tracker.service.domain.model.TaskStatus;
import java.util.List;

public interface TaskStore {
    Task findTaskById(String taskId);

    List<Task> listTasks(TaskStatus status);

    default TaskStorePage listTasksPage(TaskStatus status, int offset, int limit) {
        List<Task> tasks = listTasks(status);
        if (offset >= tasks.size()) {
            return new TaskStorePage(List.of(), false);
        }

        int toIndex = Math.min(offset + limit, tasks.size());
        return new TaskStorePage(tasks.subList(offset, toIndex), toIndex < tasks.size());
    }

    Task findCreateReplay(String idempotencyKey, String payloadHash);

    Task findStatusReplay(String taskId, String idempotencyKey, String payloadHash);

    void saveCreateReplay(String idempotencyKey, String payloadHash, Task task);

    void saveStatusReplay(String taskId, String idempotencyKey, String payloadHash, Task task);

    Task save(Task task);
}
