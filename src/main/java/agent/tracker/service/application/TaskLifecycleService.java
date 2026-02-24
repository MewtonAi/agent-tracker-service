package agent.tracker.service.application;

import agent.tracker.service.domain.contract.AssignTaskCommand;
import agent.tracker.service.domain.contract.CreateTaskCommand;
import agent.tracker.service.domain.contract.TaskLifecycleContract;
import agent.tracker.service.domain.contract.UnassignTaskCommand;
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class TaskLifecycleService implements TaskLifecycleContract {

    private final Map<String, Task> tasks = new ConcurrentHashMap<>();

    @Override
    public Task createTask(CreateTaskCommand command) {
        Instant now = Instant.now();
        String actor = coalesce(command.createdBy(), "system");
        String taskId = UUID.randomUUID().toString();

        Task task = Task.builder()
            .taskId(taskId)
            .projectId(command.projectId())
            .title(requireText(command.title(), "title"))
            .description(command.description())
            .taskType(defaultTaskType(command.taskType()))
            .status(TaskStatus.BACKLOG)
            .priority(defaultPriority(command.priority()))
            .audit(AuditMetadata.builder()
                .createdAt(now)
                .createdBy(actor)
                .updatedAt(now)
                .updatedBy(actor)
                .build())
            .build();

        tasks.put(taskId, task);
        return task;
    }

    @Override
    public Task updateTaskStatus(UpdateTaskStatusCommand command) {
        Task existing = getRequired(command.taskId());
        TaskTransitionPolicy.assertTransition(existing.getStatus(), command.status());

        Task updated = existing.toBuilder()
            .status(command.status())
            .audit(existing.getAudit().withUpdatedAt(Instant.now()).withUpdatedBy(coalesce(command.updatedBy(), "system")))
            .build();

        tasks.put(updated.getTaskId(), updated);
        return updated;
    }

    @Override
    public Task assignTask(AssignTaskCommand command) {
        Task existing = getRequired(command.taskId());
        Task updated = existing.toBuilder()
            .assignee(command.assignee())
            .audit(existing.getAudit().withUpdatedAt(Instant.now()).withUpdatedBy(coalesce(command.updatedBy(), "system")))
            .build();

        tasks.put(updated.getTaskId(), updated);
        return updated;
    }

    @Override
    public Task unassignTask(UnassignTaskCommand command) {
        Task existing = getRequired(command.taskId());
        Task updated = existing.toBuilder()
            .assignee(null)
            .audit(existing.getAudit().withUpdatedAt(Instant.now()).withUpdatedBy(coalesce(command.updatedBy(), "system")))
            .build();

        tasks.put(updated.getTaskId(), updated);
        return updated;
    }

    @Override
    public Optional<Task> getTaskById(String taskId) {
        return Optional.ofNullable(tasks.get(taskId));
    }

    @Override
    public List<Task> listTasksByProject(String projectId) {
        return tasks.values().stream()
            .filter(task -> projectId == null || projectId.isBlank() || projectId.equals(task.getProjectId()))
            .sorted(Comparator.comparing((Task t) -> t.getAudit().getUpdatedAt()).reversed())
            .toList();
    }

    private Task getRequired(String taskId) {
        return getTaskById(taskId).orElseThrow(() -> new NotFoundException("Task not found: " + taskId));
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
