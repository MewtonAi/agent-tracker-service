package agent.tracker.service.mcp;

import agent.tracker.service.application.TaskCommandService;
import agent.tracker.service.application.TaskQueryService;
import agent.tracker.service.domain.contract.CreateTaskCommand;
import agent.tracker.service.domain.contract.UpdateTaskStatusCommand;
import agent.tracker.service.domain.exception.ConcurrentModificationException;
import agent.tracker.service.domain.exception.ConflictException;
import agent.tracker.service.domain.exception.IdempotencyKeyReuseMismatchException;
import agent.tracker.service.domain.exception.InvalidTaskTransitionException;
import agent.tracker.service.domain.exception.NotFoundException;
import agent.tracker.service.domain.model.AuditMetadata;
import agent.tracker.service.domain.model.AgentRef;
import agent.tracker.service.domain.model.Task;
import agent.tracker.service.domain.model.TaskPriority;
import agent.tracker.service.domain.model.TaskStatus;
import agent.tracker.service.domain.model.TaskType;
import jakarta.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Singleton
public class TaskMcpTools {

    private final TaskCommandService commandService;
    private final TaskQueryService queryService;

    public TaskMcpTools(TaskCommandService commandService, TaskQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    public TaskToolResponse createTask(CreateTaskToolRequest request) {
        return run(() -> {
            CreateTaskToolRequest req = requireRequest(request, "createTask");
            return toResponse(commandService.createTask(new CreateTaskCommand(
                req.title(),
                req.description(),
                req.taskType(),
                req.priority(),
                req.requestedBy(),
                requireText(req.idempotencyKey(), "idempotencyKey")
            )));
        });
    }

    public TaskToolResponse getTask(GetTaskToolRequest request) {
        return run(() -> {
            GetTaskToolRequest req = requireRequest(request, "getTask");
            return toResponse(queryService.getTaskById(requireText(req.taskId(), "taskId")));
        });
    }

    public ListTasksToolResponse listTasks(ListTasksToolRequest request) {
        return run(() -> {
            ListTasksToolRequest req = requireRequest(request, "listTasks");
            return new ListTasksToolResponse(
                queryService.listTasks(parseStatusFilter(req.status())).stream().map(TaskMcpTools::toResponse).toList()
            );
        });
    }

    public TaskToolResponse updateTaskStatus(UpdateTaskStatusToolRequest request) {
        return run(() -> {
            UpdateTaskStatusToolRequest req = requireRequest(request, "updateTaskStatus");
            return toResponse(commandService.updateTaskStatus(new UpdateTaskStatusCommand(
                requireText(req.taskId(), "taskId"),
                req.status(),
                req.requestedBy(),
                requireText(req.idempotencyKey(), "idempotencyKey")
            )));
        });
    }

    private static <T> T run(CheckedSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (NotFoundException exception) {
            throw new McpToolException("TASK_NOT_FOUND", exception.getMessage(), exception);
        } catch (InvalidTaskTransitionException exception) {
            throw new McpToolException("INVALID_TASK_TRANSITION", exception.getMessage(), exception);
        } catch (ConcurrentModificationException exception) {
            throw new McpToolException("CONCURRENT_MODIFICATION", exception.getMessage(), exception);
        } catch (IdempotencyKeyReuseMismatchException exception) {
            throw new McpToolException("IDEMPOTENCY_KEY_REUSE_MISMATCH", exception.getMessage(), exception);
        } catch (ConflictException exception) {
            throw new McpToolException("TASK_CONFLICT", exception.getMessage(), exception);
        } catch (IllegalArgumentException exception) {
            throw new McpToolException("BAD_REQUEST", exception.getMessage(), exception);
        } catch (Exception exception) {
            throw new McpToolException("INTERNAL_ERROR", "An unexpected error occurred", exception);
        }
    }

    private static TaskToolResponse toResponse(Task task) {
        return new TaskToolResponse(
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

    private static <T> T requireRequest(T request, String toolName) {
        if (request == null) {
            throw new IllegalArgumentException(toolName + " request must not be null");
        }
        return request;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }

    @FunctionalInterface
    private interface CheckedSupplier<T> {
        T get();
    }

    public record CreateTaskToolRequest(
        String title,
        String description,
        TaskType taskType,
        TaskPriority priority,
        String requestedBy,
        String idempotencyKey
    ) {
    }

    public record GetTaskToolRequest(String taskId) {
    }

    public record ListTasksToolRequest(String status) {
    }

    public record UpdateTaskStatusToolRequest(
        String taskId,
        TaskStatus status,
        String requestedBy,
        String idempotencyKey
    ) {
    }

    public record ListTasksToolResponse(List<TaskToolResponse> tasks) {
    }

    public record TaskToolResponse(
        String taskId,
        String title,
        String description,
        TaskType taskType,
        TaskStatus status,
        TaskPriority priority,
        AgentRef assignee,
        AuditMetadata audit
    ) {
    }
}
