package agent.tracker.service.infrastructure.mongo;

import agent.tracker.service.application.TaskStore;
import agent.tracker.service.domain.exception.ConcurrentModificationException;
import agent.tracker.service.domain.exception.IdempotencyKeyReuseMismatchException;
import agent.tracker.service.domain.model.AgentRef;
import agent.tracker.service.domain.model.AuditMetadata;
import agent.tracker.service.domain.model.Task;
import agent.tracker.service.domain.model.TaskStatus;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.data.exceptions.OptimisticLockException;
import jakarta.inject.Singleton;
import java.time.Instant;
import java.util.List;

@Singleton
@Requires(property = "task.store", value = "mongo")
public class MongoTaskStore implements TaskStore {

    private final TaskMongoRepository taskRepository;
    private final IdempotencyMongoRepository idempotencyRepository;
    private final long idempotencyTtlHours;

    public MongoTaskStore(
        TaskMongoRepository taskRepository,
        IdempotencyMongoRepository idempotencyRepository,
        @Value("${idempotency.ttl-hours:48}") long idempotencyTtlHours
    ) {
        this.taskRepository = taskRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.idempotencyTtlHours = idempotencyTtlHours;
    }

    @Override
    public Task findTaskById(String taskId) {
        return taskRepository.findById(taskId).map(this::toDomain).orElse(null);
    }

    @Override
    public List<Task> listTasks(TaskStatus status) {
        List<TaskDocument> docs = status == null
            ? taskRepository.findAllByOrderByUpdatedAtDesc()
            : taskRepository.findByStatusOrderByUpdatedAtDesc(status);
        return docs.stream().map(this::toDomain).toList();
    }

    @Override
    public Task findCreateReplay(String idempotencyKey, String payloadHash) {
        return findReplay("create_task", idempotencyKey, payloadHash);
    }

    @Override
    public Task findStatusReplay(String taskId, String idempotencyKey, String payloadHash) {
        return findReplay(statusOperation(taskId), idempotencyKey, payloadHash);
    }

    @Override
    public void saveCreateReplay(String idempotencyKey, String payloadHash, Task task) {
        saveReplay("create_task", idempotencyKey, payloadHash, task);
    }

    @Override
    public void saveStatusReplay(String taskId, String idempotencyKey, String payloadHash, Task task) {
        saveReplay(statusOperation(taskId), idempotencyKey, payloadHash, task);
    }

    @Override
    public Task save(Task task) {
        try {
            TaskDocument saved = taskRepository.save(toDocument(task));
            return toDomain(saved);
        } catch (OptimisticLockException e) {
            throw new ConcurrentModificationException("Task update conflict: " + task.getTaskId());
        }
    }

    private Task findReplay(String operation, String key, String payloadHash) {
        return idempotencyRepository.findByOperationAndKey(operation, key)
            .map(record -> ensurePayloadMatch(record, key, payloadHash))
            .map(IdempotencyRecordDocument::resultRef)
            .map(this::findTaskById)
            .orElse(null);
    }

    private IdempotencyRecordDocument ensurePayloadMatch(IdempotencyRecordDocument record, String key, String payloadHash) {
        if (!record.payloadHash().equals(payloadHash)) {
            throw new IdempotencyKeyReuseMismatchException("Idempotency key was already used with a different payload: " + key);
        }
        return record;
    }

    private void saveReplay(String operation, String key, String payloadHash, Task task) {
        if (idempotencyRepository.findByOperationAndKey(operation, key).isPresent()) {
            return;
        }

        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(idempotencyTtlHours * 3600);
        IdempotencyRecordDocument document = new IdempotencyRecordDocument(
            operation + ":" + key,
            operation,
            key,
            payloadHash,
            task.getTaskId(),
            expiresAt,
            now,
            now
        );

        try {
            idempotencyRepository.save(document);
        } catch (RuntimeException exception) {
            if (!isDuplicateIdempotencyKey(exception)) {
                throw exception;
            }
            // race on first insert; read-path remains idempotent
        }
    }

    private Task toDomain(TaskDocument doc) {
        return Task.builder()
            .taskId(doc.taskId())
            .version(doc.version())
            .title(doc.title())
            .description(doc.description())
            .taskType(doc.taskType())
            .status(doc.status())
            .priority(doc.priority())
            .assignee(toDomain(doc.assignee()))
            .audit(AuditMetadata.builder()
                .createdAt(doc.createdAt())
                .createdBy(doc.createdBy())
                .updatedAt(doc.updatedAt())
                .updatedBy(doc.updatedBy())
                .build())
            .build();
    }

    private TaskDocument toDocument(Task task) {
        return new TaskDocument(
            task.getTaskId(),
            task.getVersion(),
            task.getTitle(),
            task.getDescription(),
            task.getTaskType(),
            task.getStatus(),
            task.getPriority(),
            toDoc(task.getAssignee()),
            task.getAudit().getCreatedAt(),
            task.getAudit().getCreatedBy(),
            task.getAudit().getUpdatedAt(),
            task.getAudit().getUpdatedBy()
        );
    }

    private AgentRef toDomain(AgentRefEmbeddable embeddable) {
        if (embeddable == null) {
            return null;
        }
        return AgentRef.builder()
            .agentId(embeddable.agentId())
            .displayName(embeddable.displayName())
            .capabilities(embeddable.capabilities())
            .build();
    }

    private AgentRefEmbeddable toDoc(AgentRef ref) {
        if (ref == null) {
            return null;
        }
        return new AgentRefEmbeddable(ref.agentId(), ref.displayName(), ref.capabilities());
    }

    private static String statusOperation(String taskId) {
        return "update_task_status:" + taskId;
    }

    private static boolean isDuplicateIdempotencyKey(RuntimeException exception) {
        String message = exception.getMessage();
        return message != null && message.contains("E11000");
    }
}
