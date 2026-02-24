package agent.tracker.service.api;

import agent.tracker.service.api.dto.AssignTaskRequest;
import agent.tracker.service.api.dto.CreateTaskRequest;
import agent.tracker.service.api.dto.TaskResponse;
import agent.tracker.service.api.dto.UpdateTaskStatusRequest;
import agent.tracker.service.domain.contract.AssignTaskCommand;
import agent.tracker.service.domain.contract.CreateTaskCommand;
import agent.tracker.service.domain.contract.TaskLifecycleContract;
import agent.tracker.service.domain.contract.UnassignTaskCommand;
import agent.tracker.service.domain.contract.UpdateTaskStatusCommand;
import agent.tracker.service.domain.model.AgentRef;
import agent.tracker.service.domain.model.Task;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Patch;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Controller("/v1/tasks")
public class TaskController {

    private final TaskLifecycleContract taskLifecycle;

    public TaskController(TaskLifecycleContract taskLifecycle) {
        this.taskLifecycle = taskLifecycle;
    }

    @Post
    public TaskResponse create(@Valid @Body CreateTaskRequest request) {
        Task task = taskLifecycle.createTask(new CreateTaskCommand(
            request.projectId(),
            request.title(),
            request.description(),
            request.taskType(),
            request.priority(),
            request.requestedBy()
        ));
        return toResponse(task);
    }

    @Get("/{taskId}")
    public TaskResponse getById(@PathVariable @NotBlank String taskId) {
        return taskLifecycle.getTaskById(taskId)
            .map(TaskController::toResponse)
            .orElseThrow(() -> new agent.tracker.service.domain.exception.NotFoundException("Task not found: " + taskId));
    }

    @Get
    public List<TaskResponse> list(@QueryValue(defaultValue = "") String projectId) {
        String filter = projectId.isBlank() ? null : projectId;
        return taskLifecycle.listTasksByProject(filter).stream().map(TaskController::toResponse).toList();
    }

    @Patch("/{taskId}/status")
    public TaskResponse updateStatus(@PathVariable String taskId, @Valid @Body UpdateTaskStatusRequest request) {
        Task task = taskLifecycle.updateTaskStatus(new UpdateTaskStatusCommand(taskId, request.status(), request.requestedBy()));
        return toResponse(task);
    }

    @Patch("/{taskId}/assign")
    public TaskResponse assign(@PathVariable String taskId, @Valid @Body AssignTaskRequest request) {
        AgentRef assignee = AgentRef.builder()
            .agentId(request.agentId())
            .displayName(request.agentDisplayName())
            .capabilities(request.capabilities())
            .build();

        Task task = taskLifecycle.assignTask(new AssignTaskCommand(taskId, assignee, request.requestedBy()));
        return toResponse(task);
    }

    @Patch("/{taskId}/unassign")
    public TaskResponse unassign(@PathVariable String taskId, @Body AssignTaskRequest request) {
        String actor = request == null ? "system" : request.requestedBy();
        Task task = taskLifecycle.unassignTask(new UnassignTaskCommand(taskId, actor));
        return toResponse(task);
    }

    private static TaskResponse toResponse(Task task) {
        return new TaskResponse(
            task.getTaskId(),
            task.getProjectId(),
            task.getTitle(),
            task.getDescription(),
            task.getTaskType(),
            task.getStatus(),
            task.getPriority(),
            task.getAssignee(),
            task.getAudit()
        );
    }
}
