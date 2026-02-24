package agent.tracker.service.api;

import agent.tracker.service.api.dto.CreateTaskRequest;
import agent.tracker.service.api.dto.ListTasksResponse;
import agent.tracker.service.api.dto.TaskResponse;
import agent.tracker.service.api.dto.UpdateTaskStatusRequest;
import agent.tracker.service.application.TaskCommandService;
import agent.tracker.service.application.TaskListPage;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Arrays;
import java.util.Locale;

@Tag(name = "Tasks")
@Controller("/v1/tasks")
public class TaskController {

    private final TaskCommandService commandService;
    private final TaskQueryService queryService;

    public TaskController(TaskCommandService commandService, TaskQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @Operation(summary = "Create task")
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

    @Operation(summary = "Get task by id")
    @Get("/{taskId}")
    public TaskResponse getById(@PathVariable @NotBlank String taskId) {
        return toResponse(queryService.getTaskById(taskId));
    }

    @Operation(summary = "List tasks")
    @Get
    public ListTasksResponse list(
        @QueryValue(defaultValue = "") String status,
        @QueryValue(defaultValue = "") String cursor,
        @QueryValue(defaultValue = "50") Integer limit
    ) {
        TaskStatus filter = parseStatusFilter(status);
        TaskListPage page = queryService.listTasks(filter, cursor, limit);
        return new ListTasksResponse(page.tasks().stream().map(TaskController::toResponse).toList(), page.nextCursor());
    }

    @Operation(summary = "Update task status")
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

    private static TaskStatus parseStatusFilter(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return TaskStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            String allowed = Arrays.stream(TaskStatus.values()).map(Enum::name).sorted().reduce((left, right) -> left + ", " + right).orElse("");
            throw new IllegalArgumentException("status must be one of [" + allowed + "]");
        }
    }
}
