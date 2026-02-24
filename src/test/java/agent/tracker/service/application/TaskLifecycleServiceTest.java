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

    private final TaskLifecycleService service = new TaskLifecycleService();

    @Test
    void shouldCreateTaskWithDefaults() {
        Task task = service.createTask(new CreateTaskCommand("proj-1", "Implement endpoint", null, null, null, "owner"));

        assertNotNull(task.getTaskId());
        assertEquals(TaskStatus.BACKLOG, task.getStatus());
        assertEquals(TaskPriority.MEDIUM, task.getPriority());
        assertEquals(TaskType.FEATURE, task.getTaskType());
        assertEquals("owner", task.getAudit().getCreatedBy());
    }

    @Test
    void shouldRejectInvalidTransition() {
        Task task = service.createTask(new CreateTaskCommand("proj-1", "Invalid transition test", null, TaskType.BUG, TaskPriority.HIGH, "owner"));

        assertThrows(InvalidTaskTransitionException.class, () ->
            service.updateTaskStatus(new UpdateTaskStatusCommand(task.getTaskId(), TaskStatus.DONE, "owner"))
        );
    }

    @Test
    void shouldAllowValidTransitionPath() {
        Task task = service.createTask(new CreateTaskCommand("proj-1", "Happy path", null, TaskType.FEATURE, TaskPriority.MEDIUM, "owner"));

        Task ready = service.updateTaskStatus(new UpdateTaskStatusCommand(task.getTaskId(), TaskStatus.READY, "owner"));
        Task inProgress = service.updateTaskStatus(new UpdateTaskStatusCommand(task.getTaskId(), TaskStatus.IN_PROGRESS, "owner"));
        Task inReview = service.updateTaskStatus(new UpdateTaskStatusCommand(task.getTaskId(), TaskStatus.IN_REVIEW, "owner"));
        Task done = service.updateTaskStatus(new UpdateTaskStatusCommand(task.getTaskId(), TaskStatus.DONE, "owner"));

        assertEquals(TaskStatus.READY, ready.getStatus());
        assertEquals(TaskStatus.IN_PROGRESS, inProgress.getStatus());
        assertEquals(TaskStatus.IN_REVIEW, inReview.getStatus());
        assertEquals(TaskStatus.DONE, done.getStatus());
    }
}
