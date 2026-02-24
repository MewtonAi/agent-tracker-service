package agent.tracker.service.application;

import agent.tracker.service.domain.contract.CreateTaskCommand;
import agent.tracker.service.domain.contract.UpdateTaskStatusCommand;
import agent.tracker.service.domain.exception.NotFoundException;
import agent.tracker.service.domain.model.AuditMetadata;
import agent.tracker.service.domain.model.Task;
import agent.tracker.service.domain.model.TaskPriority;
import agent.tracker.service.domain.model.TaskStatus;
import agent.tracker.service.domain.model.TaskType;
import agent.tracker.service.domain.policy.TaskTransitionPolicy;
import jakarta.inject.Singleton;
import java.time.Instant;
import java.util.UUID;

@Singleton
public class TaskCommandService {

    private final TaskStore store;

    public TaskCommandService(TaskStore store) {
        this.store = store;
    }

    public Task createTask(CreateTaskCommand command) {
        String idempotencyKey = requireText(command.idempotencyKey(), "idempotencyKey");
        Task existing = store.findCreateReplay(idempotencyKey);
        if (existing != null) {
            return existing;
        }

        Instant now = Instant.now();
        String actor = coalesce(command.createdBy(), "system");
        String taskId = UUID.randomUUID().toString();

        Task task = Task.builder()
            .taskId(taskId)
            .title(requireText(command.title(), "title"))
            .description(command.description())
            .taskType(defaultTaskType(command.taskType()))
            .status(TaskStatus.NEW)
            .priority(defaultPriority(command.priority()))
            .audit(AuditMetadata.builder()
                .createdAt(now)
                .createdBy(actor)
                .updatedAt(now)
                .updatedBy(actor)
                .build())
            .build();

        Task saved = store.save(task);
        store.saveCreateReplay(idempotencyKey, saved);
        return saved;
    }

    public Task updateTaskStatus(UpdateTaskStatusCommand command) {
        String idempotencyKey = requireText(command.idempotencyKey(), "idempotencyKey");
        Task existingReplay = store.findStatusReplay(idempotencyKey);
        if (existingReplay != null) {
            return existingReplay;
        }

        Task existing = store.findTaskById(command.taskId());
        if (existing == null) {
            throw new NotFoundException("Task not found: " + command.taskId());
        }
        TaskTransitionPolicy.assertTransition(existing.getStatus(), command.status());

        Task updated = existing.toBuilder()
            .status(command.status())
            .audit(existing.getAudit().withUpdatedAt(Instant.now()).withUpdatedBy(coalesce(command.updatedBy(), "system")))
            .build();

        Task saved = store.save(updated);
        store.saveStatusReplay(idempotencyKey, saved);
        return saved;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }

    private static TaskType defaultTaskType(TaskType taskType) {
        return taskType == null ? TaskType.FEATURE : taskType;
    }

    private static TaskPriority defaultPriority(TaskPriority priority) {
        return priority == null ? TaskPriority.MEDIUM : priority;
    }

    private static String coalesce(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
