package agent.tracker.service.application;

import agent.tracker.service.domain.contract.CreateTaskCommand;
import agent.tracker.service.domain.contract.UpdateTaskStatusCommand;
import agent.tracker.service.domain.exception.InvalidTaskTransitionException;
import agent.tracker.service.domain.model.Task;
import agent.tracker.service.domain.model.TaskPriority;
import agent.tracker.service.domain.model.TaskStatus;
import agent.tracker.service.domain.model.TaskType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskLifecycleServiceTest {

    private final InMemoryTaskStore store = new InMemoryTaskStore();
    private final TaskCommandService commandService = new TaskCommandService(store);

    @Test
    void shouldCreateTaskWithDefaultsAndIdempotency() {
        Task first = commandService.createTask(new CreateTaskCommand("Implement endpoint", null, null, null, "owner", "idem-create-1"));
        Task replay = commandService.createTask(new CreateTaskCommand("Ignored on replay", null, TaskType.BUG, TaskPriority.HIGH, "owner", "idem-create-1"));

        assertEquals(first.getTaskId(), replay.getTaskId());
        assertEquals(TaskStatus.NEW, first.getStatus());
        assertEquals(TaskPriority.MEDIUM, first.getPriority());
        assertEquals(TaskType.FEATURE, first.getTaskType());
        assertEquals("owner", first.getAudit().getCreatedBy());
    }

    @Test
    void shouldRejectInvalidTransition() {
        Task task = commandService.createTask(new CreateTaskCommand("Invalid transition test", null, TaskType.BUG, TaskPriority.HIGH, "owner", "idem-create-2"));

        assertThrows(InvalidTaskTransitionException.class, () ->
            commandService.updateTaskStatus(new UpdateTaskStatusCommand(task.getTaskId(), TaskStatus.DONE, "owner", "idem-status-1"))
        );
    }

    @Test
    void shouldAllowValidTransitionPathAndStatusIdempotency() {
        Task task = commandService.createTask(new CreateTaskCommand("Happy path", null, TaskType.FEATURE, TaskPriority.MEDIUM, "owner", "idem-create-3"));

        Task inProgress = commandService.updateTaskStatus(new UpdateTaskStatusCommand(task.getTaskId(), TaskStatus.IN_PROGRESS, "owner", "idem-status-2"));
        Task done = commandService.updateTaskStatus(new UpdateTaskStatusCommand(task.getTaskId(), TaskStatus.DONE, "owner", "idem-status-3"));
        Task replay = commandService.updateTaskStatus(new UpdateTaskStatusCommand(task.getTaskId(), TaskStatus.BLOCKED, "owner", "idem-status-3"));

        assertEquals(TaskStatus.IN_PROGRESS, inProgress.getStatus());
        assertEquals(TaskStatus.DONE, done.getStatus());
        assertEquals(TaskStatus.DONE, replay.getStatus());
    }
}
