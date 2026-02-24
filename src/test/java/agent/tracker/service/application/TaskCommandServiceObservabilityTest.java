package agent.tracker.service.application;

import agent.tracker.service.domain.contract.CreateTaskCommand;
import agent.tracker.service.domain.contract.UpdateTaskStatusCommand;
import agent.tracker.service.domain.exception.IdempotencyKeyReuseMismatchException;
import agent.tracker.service.domain.model.Task;
import agent.tracker.service.domain.model.TaskPriority;
import agent.tracker.service.domain.model.TaskStatus;
import agent.tracker.service.domain.model.TaskType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskCommandServiceObservabilityTest {

    @Test
    void shouldEmitFirstWriteAndReplayHitForCreate() {
        InMemoryTaskStore store = new InMemoryTaskStore();
        RecordingTelemetry telemetry = new RecordingTelemetry();
        TaskCommandService service = new TaskCommandService(store, telemetry);

        Task first = service.createTask(new CreateTaskCommand("Task A", "desc", TaskType.FEATURE, TaskPriority.HIGH, "qa", "obs-create-1"));
        Task replay = service.createTask(new CreateTaskCommand("Task A", "desc", TaskType.FEATURE, TaskPriority.HIGH, "qa", "obs-create-1"));

        assertNotNull(first.getTaskId());
        assertEquals(first.getTaskId(), replay.getTaskId());
        assertEquals(List.of("first_write:create_task", "replay_hit:create_task"), telemetry.events);
    }

    @Test
    void shouldEmitMismatchRejectForCreateMismatch() {
        InMemoryTaskStore store = new InMemoryTaskStore();
        RecordingTelemetry telemetry = new RecordingTelemetry();
        TaskCommandService service = new TaskCommandService(store, telemetry);

        service.createTask(new CreateTaskCommand("Task A", "desc", TaskType.FEATURE, TaskPriority.HIGH, "qa", "obs-create-2"));

        assertThrows(IdempotencyKeyReuseMismatchException.class, () ->
            service.createTask(new CreateTaskCommand("Task B", "desc", TaskType.FEATURE, TaskPriority.HIGH, "qa", "obs-create-2"))
        );

        assertEquals(List.of("first_write:create_task", "mismatch_reject:create_task"), telemetry.events);
    }

    @Test
    void shouldEmitFirstWriteAndReplayHitForStatusUpdate() {
        InMemoryTaskStore store = new InMemoryTaskStore();
        RecordingTelemetry telemetry = new RecordingTelemetry();
        TaskCommandService service = new TaskCommandService(store, telemetry);

        Task created = service.createTask(new CreateTaskCommand("Task A", "desc", TaskType.FEATURE, TaskPriority.HIGH, "qa", "obs-status-create"));
        telemetry.events.clear();

        Task updated = service.updateTaskStatus(new UpdateTaskStatusCommand(created.getTaskId(), TaskStatus.IN_PROGRESS, "qa", "obs-status-1"));
        Task replay = service.updateTaskStatus(new UpdateTaskStatusCommand(created.getTaskId(), TaskStatus.IN_PROGRESS, "qa", "obs-status-1"));

        assertEquals(TaskStatus.IN_PROGRESS, updated.getStatus());
        assertEquals(updated.getTaskId(), replay.getTaskId());
        assertEquals(List.of("first_write:update_task_status", "replay_hit:update_task_status"), telemetry.events);
    }

    private static final class RecordingTelemetry implements IdempotencyTelemetry {
        private final List<String> events = new ArrayList<>();

        @Override
        public void firstWrite(String operation) {
            events.add("first_write:" + operation);
        }

        @Override
        public void replayHit(String operation) {
            events.add("replay_hit:" + operation);
        }

        @Override
        public void mismatchReject(String operation) {
            events.add("mismatch_reject:" + operation);
        }
    }
}
