package agent.tracker.service.mcp;

import agent.tracker.service.application.TaskCommandService;
import agent.tracker.service.application.TaskListPage;
import agent.tracker.service.application.TaskQueryService;
import agent.tracker.service.application.contract.CorrelationIdNormalizer;
import agent.tracker.service.application.contract.TaskInputNormalizer;
import agent.tracker.service.domain.contract.CreateTaskCommand;
import agent.tracker.service.domain.contract.UpdateTaskStatusCommand;
import agent.tracker.service.domain.exception.ConcurrentModificationException;
import agent.tracker.service.domain.exception.ConflictException;
import agent.tracker.service.domain.exception.IdempotencyKeyReuseMismatchException;
import agent.tracker.service.domain.exception.InvalidTaskTransitionException;
import agent.tracker.service.domain.exception.NotFoundException;
import agent.tracker.service.domain.model.AgentRef;
import agent.tracker.service.domain.model.AuditMetadata;
import agent.tracker.service.domain.model.Task;
import agent.tracker.service.domain.model.TaskPriority;
import agent.tracker.service.domain.model.TaskStatus;
import agent.tracker.service.domain.model.TaskType;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class TaskMcpTools {

    private final TaskCommandService commandService;
    private final TaskQueryService queryService;
    private final TaskInputNormalizer inputNormalizer;
    private final CorrelationIdNormalizer correlationIdNormalizer;

    public TaskMcpTools(
        TaskCommandService commandService,
        TaskQueryService queryService,
        TaskInputNormalizer inputNormalizer,
        CorrelationIdNormalizer correlationIdNormalizer
    ) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.inputNormalizer = inputNormalizer;
        this.correlationIdNormalizer = correlationIdNormalizer;
    }

    public TaskToolResponse createTask(CreateTaskToolRequest request) {
        CreateTaskToolRequest req = requireRequest(request, "createTask");
        return run(req.correlationId(), () -> toResponse(commandService.createTask(new CreateTaskCommand(
            req.title(),
            req.description(),
            req.taskType(),
            req.priority(),
            req.requestedBy(),
            inputNormalizer.requireIdempotencyKey(req.idempotencyKey())
        ))));
    }

    public TaskToolResponse getTask(GetTaskToolRequest request) {
        GetTaskToolRequest req = requireRequest(request, "getTask");
        return run(req.correlationId(), () -> toResponse(queryService.getTaskById(inputNormalizer.requireText(req.taskId(), "taskId"))));
    }

    public ListTasksToolResponse listTasks(ListTasksToolRequest request) {
        ListTasksToolRequest req = requireRequest(request, "listTasks");
        return run(req.correlationId(), () -> {
            TaskStatus filter = inputNormalizer.parseStatusFilter(req.status());
            String cursor = inputNormalizer.normalizeCursor(req.cursor());
            int limit = inputNormalizer.normalizeLimit(req.limit());
            TaskListPage page = queryService.listTasks(filter, cursor, limit);
            return new ListTasksToolResponse(page.tasks().stream().map(TaskMcpTools::toResponse).toList(), page.nextCursor());
        });
    }

    public TaskToolResponse updateTaskStatus(UpdateTaskStatusToolRequest request) {
        UpdateTaskStatusToolRequest req = requireRequest(request, "updateTaskStatus");
        return run(req.correlationId(), () -> toResponse(commandService.updateTaskStatus(new UpdateTaskStatusCommand(
            inputNormalizer.requireText(req.taskId(), "taskId"),
            req.status(),
            req.requestedBy(),
            inputNormalizer.requireIdempotencyKey(req.idempotencyKey())
        ))));
    }

    private <T> T run(String requestedCorrelationId, CheckedSupplier<T> supplier) {
        String correlationId = correlationIdNormalizer.normalizeOrGenerate(requestedCorrelationId);
        try {
            return supplier.get();
        } catch (NotFoundException exception) {
            throw new McpToolException("TASK_NOT_FOUND", exception.getMessage(), correlationId, exception);
        } catch (InvalidTaskTransitionException exception) {
            throw new McpToolException("INVALID_TASK_TRANSITION", exception.getMessage(), correlationId, exception);
        } catch (ConcurrentModificationException exception) {
            throw new McpToolException("CONCURRENT_MODIFICATION", exception.getMessage(), correlationId, exception);
        } catch (IdempotencyKeyReuseMismatchException exception) {
            throw new McpToolException("IDEMPOTENCY_KEY_REUSE_MISMATCH", exception.getMessage(), correlationId, exception);
        } catch (ConflictException exception) {
            throw new McpToolException("TASK_CONFLICT", exception.getMessage(), correlationId, exception);
        } catch (IllegalArgumentException exception) {
            throw new McpToolException("BAD_REQUEST", exception.getMessage(), correlationId, exception);
        } catch (Exception exception) {
            throw new McpToolException("INTERNAL_ERROR", "An unexpected error occurred", correlationId, exception);
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

    private static <T> T requireRequest(T request, String toolName) {
        if (request == null) {
            throw new IllegalArgumentException(toolName + " request must not be null");
        }
        return request;
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
        String idempotencyKey,
        String correlationId
    ) {
    }

    public record GetTaskToolRequest(String taskId, String correlationId) {
    }

    public record ListTasksToolRequest(String status, String cursor, Integer limit, String correlationId) {
    }

    public record UpdateTaskStatusToolRequest(
        String taskId,
        TaskStatus status,
        String requestedBy,
        String idempotencyKey,
        String correlationId
    ) {
    }

    public record ListTasksToolResponse(List<TaskToolResponse> tasks, String nextCursor) {
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
