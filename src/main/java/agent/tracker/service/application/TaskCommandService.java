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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
        String payloadHash = hashPayload("create", command.title(), command.description(), command.taskType(), command.priority(), command.createdBy());
        Task existing = store.findCreateReplay(idempotencyKey, payloadHash);
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
        store.saveCreateReplay(idempotencyKey, payloadHash, saved);
        return saved;
    }

    public Task updateTaskStatus(UpdateTaskStatusCommand command) {
        String idempotencyKey = requireText(command.idempotencyKey(), "idempotencyKey");
        String payloadHash = hashPayload("update-status", command.taskId(), command.status(), command.updatedBy());
        Task existingReplay = store.findStatusReplay(command.taskId(), idempotencyKey, payloadHash);
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
        store.saveStatusReplay(command.taskId(), idempotencyKey, payloadHash, saved);
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

    private static String hashPayload(Object... parts) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (Object part : parts) {
                String value = part == null ? "<null>" : String.valueOf(part).trim();
                digest.update(value.getBytes(StandardCharsets.UTF_8));
                digest.update((byte) '|');
            }
            byte[] hash = digest.digest();
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Missing SHA-256 support", e);
        }
    }
}
