package agent.tracker.service.domain.contract;

import agent.tracker.service.domain.model.Task;
import java.util.List;
import java.util.Optional;

public interface TaskLifecycleContract {

    Task createTask(CreateTaskCommand command);

    Task updateTaskStatus(UpdateTaskStatusCommand command);

    Task assignTask(AssignTaskCommand command);

    Task unassignTask(UnassignTaskCommand command);

    Optional<Task> getTaskById(String taskId);

    List<Task> listTasksByProject(String projectId);
}
