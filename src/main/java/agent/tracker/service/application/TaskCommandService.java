package agent.tracker.service.application;

import agent.tracker.service.domain.contract.CreateTaskCommand;
import agent.tracker.service.domain.contract.UpdateTaskStatusCommand;
import agent.tracker.service.domain.exception.IdempotencyKeyReuseMismatchException;
import agent.tracker.service.domain.exception.NotFoundException;
import agent.tracker.service.domain.model.AuditMetadata;
import agent.tracker.service.domain.model.Task;
import agent.tracker.service.domain.model.TaskPriority;
import agent.tracker.service.domain.model.TaskStatus;
import agent.tracker.service.domain.model.TaskType;
import agent.tracker.service.domain.policy.TaskTransitionPolicy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.UUID;

@Singleton
public class TaskCommandService {

    private static final String OP_CREATE_TASK = "create_task";
    private static final String OP_UPDATE_TASK_STATUS = "update_task_status";

    private final TaskStore store;
    private final IdempotencyTelemetry idempotencyTelemetry;

    @Inject
    public TaskCommandService(TaskStore store, IdempotencyTelemetry idempotencyTelemetry) {
        this.store = store;
        this.idempotencyTelemetry = idempotencyTelemetry;
    }

    public TaskCommandService(TaskStore store) {
        this(store, new IdempotencyTelemetry() {
            @Override
            public void firstWrite(String operation) {
            }

            @Override
            public void replayHit(String operation) {
            }

            @Override
            public void mismatchReject(String operation) {
            }
        });
    }

    public Task createTask(CreateTaskCommand command) {
        String idempotencyKey = requireText(command.idempotencyKey(), "idempotencyKey");
        String payloadHash = hashPayload("create", command.title(), command.description(), command.taskType(), command.priority(), command.createdBy());

        Task existing = findCreateReplay(idempotencyKey, payloadHash);
        if (existing != null) {
            idempotencyTelemetry.replayHit(OP_CREATE_TASK);
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
        idempotencyTelemetry.firstWrite(OP_CREATE_TASK);
        return saved;
    }

    public Task updateTaskStatus(UpdateTaskStatusCommand command) {
        String idempotencyKey = requireText(command.idempotencyKey(), "idempotencyKey");
        String payloadHash = hashPayload("update-status", command.taskId(), command.status(), command.updatedBy());

        Task existingReplay = findStatusReplay(command.taskId(), idempotencyKey, payloadHash);
        if (existingReplay != null) {
            idempotencyTelemetry.replayHit(OP_UPDATE_TASK_STATUS);
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
        idempotencyTelemetry.firstWrite(OP_UPDATE_TASK_STATUS);
        return saved;
    }

    private Task findCreateReplay(String idempotencyKey, String payloadHash) {
        try {
            return store.findCreateReplay(idempotencyKey, payloadHash);
        } catch (IdempotencyKeyReuseMismatchException exception) {
            idempotencyTelemetry.mismatchReject(OP_CREATE_TASK);
            throw exception;
        }
    }

    private Task findStatusReplay(String taskId, String idempotencyKey, String payloadHash) {
        try {
            return store.findStatusReplay(taskId, idempotencyKey, payloadHash);
        } catch (IdempotencyKeyReuseMismatchException exception) {
            idempotencyTelemetry.mismatchReject(OP_UPDATE_TASK_STATUS);
            throw exception;
        }
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
