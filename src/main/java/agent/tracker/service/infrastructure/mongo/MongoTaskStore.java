package agent.tracker.service.infrastructure.mongo;

import agent.tracker.service.application.TaskStore;
import agent.tracker.service.domain.exception.ConflictException;
import agent.tracker.service.domain.model.AgentRef;
import agent.tracker.service.domain.model.AuditMetadata;
import agent.tracker.service.domain.model.Task;
import agent.tracker.service.domain.model.TaskStatus;
import io.micronaut.context.annotation.Requires;
import io.micronaut.data.exceptions.OptimisticLockException;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
@Requires(property = "task.store", value = "mongo")
public class MongoTaskStore implements TaskStore {

    private final TaskMongoRepository taskRepository;
    private final IdempotencyMongoRepository idempotencyRepository;

    public MongoTaskStore(TaskMongoRepository taskRepository, IdempotencyMongoRepository idempotencyRepository) {
        this.taskRepository = taskRepository;
        this.idempotencyRepository = idempotencyRepository;
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
    public Task findCreateReplay(String idempotencyKey) {
        return findReplay("create:" + idempotencyKey);
    }

    @Override
    public Task findStatusReplay(String idempotencyKey) {
        return findReplay("status:" + idempotencyKey);
    }

    @Override
    public void saveCreateReplay(String idempotencyKey, Task task) {
        saveReplay("create:" + idempotencyKey, task);
    }

    @Override
    public void saveStatusReplay(String idempotencyKey, Task task) {
        saveReplay("status:" + idempotencyKey, task);
    }

    @Override
    public Task save(Task task) {
        try {
            TaskDocument saved = taskRepository.save(toDocument(task));
            return toDomain(saved);
        } catch (OptimisticLockException e) {
            throw new ConflictException("Task update conflict: " + task.getTaskId());
        }
    }

    private Task findReplay(String key) {
        return idempotencyRepository.findById(key)
            .map(IdempotencyRecordDocument::taskId)
            .map(this::findTaskById)
            .orElse(null);
    }

    private void saveReplay(String key, Task task) {
        if (idempotencyRepository.findById(key).isPresent()) {
            return;
        }
        try {
            idempotencyRepository.save(new IdempotencyRecordDocument(key, task.getTaskId(), null));
        } catch (RuntimeException ignored) {
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
}
