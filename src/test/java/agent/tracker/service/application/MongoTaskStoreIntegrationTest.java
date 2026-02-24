package agent.tracker.service.application;

import agent.tracker.service.domain.contract.CreateTaskCommand;
import agent.tracker.service.domain.contract.UpdateTaskStatusCommand;
import agent.tracker.service.domain.model.Task;
import agent.tracker.service.domain.model.TaskPriority;
import agent.tracker.service.domain.model.TaskStatus;
import agent.tracker.service.domain.model.TaskType;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@MicronautTest
@Property(name = "task.store", value = "mongo")
class MongoTaskStoreIntegrationTest {

    @Inject
    TaskCommandService commandService;

    @Inject
    TaskQueryService queryService;

    @Test
    void shouldPersistAndReplayIdempotentMutations() {
        Task created = commandService.createTask(new CreateTaskCommand(
            "Persist me",
            "mongo",
            TaskType.FEATURE,
            TaskPriority.HIGH,
            "integration",
            "mongo-create-1"
        ));

        Task replay = commandService.createTask(new CreateTaskCommand(
            "ignored",
            null,
            null,
            null,
            "integration",
            "mongo-create-1"
        ));

        assertEquals(created.getTaskId(), replay.getTaskId());
        assertNotNull(queryService.getTaskById(created.getTaskId()));

        Task updated = commandService.updateTaskStatus(new UpdateTaskStatusCommand(
            created.getTaskId(),
            TaskStatus.IN_PROGRESS,
            "integration",
            "mongo-status-1"
        ));

        Task statusReplay = commandService.updateTaskStatus(new UpdateTaskStatusCommand(
            created.getTaskId(),
            TaskStatus.BLOCKED,
            "integration",
            "mongo-status-1"
        ));

        assertEquals(TaskStatus.IN_PROGRESS, updated.getStatus());
        assertEquals(TaskStatus.IN_PROGRESS, statusReplay.getStatus());
    }

    @Test
    void shouldScopeStatusIdempotencyByTaskId() {
        Task first = commandService.createTask(new CreateTaskCommand(
            "first",
            "mongo",
            TaskType.FEATURE,
            TaskPriority.HIGH,
            "integration",
            "mongo-create-scope-1"
        ));

        Task second = commandService.createTask(new CreateTaskCommand(
            "second",
            "mongo",
            TaskType.FEATURE,
            TaskPriority.HIGH,
            "integration",
            "mongo-create-scope-2"
        ));

        Task firstUpdate = commandService.updateTaskStatus(new UpdateTaskStatusCommand(
            first.getTaskId(),
            TaskStatus.IN_PROGRESS,
            "integration",
            "shared-mongo-status-key"
        ));

        Task secondUpdate = commandService.updateTaskStatus(new UpdateTaskStatusCommand(
            second.getTaskId(),
            TaskStatus.IN_PROGRESS,
            "integration",
            "shared-mongo-status-key"
        ));

        assertEquals(TaskStatus.IN_PROGRESS, firstUpdate.getStatus());
        assertEquals(TaskStatus.IN_PROGRESS, secondUpdate.getStatus());
    }
}
