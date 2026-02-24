package agent.tracker.service.api;

import agent.tracker.service.api.dto.CreateTaskRequest;
import agent.tracker.service.api.dto.TaskResponse;
import agent.tracker.service.api.dto.UpdateTaskStatusRequest;
import agent.tracker.service.application.TaskCommandService;
import agent.tracker.service.application.TaskQueryService;
import agent.tracker.service.domain.contract.CreateTaskCommand;
import agent.tracker.service.domain.contract.UpdateTaskStatusCommand;
import agent.tracker.service.domain.model.Task;
import agent.tracker.service.domain.model.TaskStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Patch;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Controller("/v1/tasks")
public class TaskController {

    private final TaskCommandService commandService;
    private final TaskQueryService queryService;

    public TaskController(TaskCommandService commandService, TaskQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @Post
    public TaskResponse create(
        @Header("Idempotency-Key") @NotBlank String idempotencyKey,
        @Valid @Body CreateTaskRequest request
    ) {
        Task task = commandService.createTask(new CreateTaskCommand(
            request.title(),
            request.description(),
            request.taskType(),
            request.priority(),
            request.requestedBy(),
            idempotencyKey
        ));
        return toResponse(task);
    }

    @Get("/{taskId}")
    public TaskResponse getById(@PathVariable @NotBlank String taskId) {
        return toResponse(queryService.getTaskById(taskId));
    }

    @Get
    public List<TaskResponse> list(@QueryValue(defaultValue = "") String status) {
        TaskStatus filter = status.isBlank() ? null : TaskStatus.valueOf(status);
        return queryService.listTasks(filter).stream().map(TaskController::toResponse).toList();
    }

    @Patch("/{taskId}/status")
    public TaskResponse updateStatus(
        @PathVariable String taskId,
        @Header("Idempotency-Key") @NotBlank String idempotencyKey,
        @Valid @Body UpdateTaskStatusRequest request
    ) {
        Task task = commandService.updateTaskStatus(new UpdateTaskStatusCommand(taskId, request.status(), request.requestedBy(), idempotencyKey));
        return toResponse(task);
    }

    private static TaskResponse toResponse(Task task) {
        return new TaskResponse(
            task.getTaskId(),
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
