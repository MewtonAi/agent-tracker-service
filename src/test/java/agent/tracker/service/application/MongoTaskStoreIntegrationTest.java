package agent.tracker.service.application;

import agent.tracker.service.domain.contract.CreateTaskCommand;
import agent.tracker.service.domain.contract.UpdateTaskStatusCommand;
import agent.tracker.service.domain.exception.IdempotencyKeyReuseMismatchException;
import agent.tracker.service.domain.model.Task;
import agent.tracker.service.domain.model.TaskPriority;
import agent.tracker.service.domain.model.TaskStatus;
import agent.tracker.service.domain.model.TaskType;
import agent.tracker.service.infrastructure.mongo.IdempotencyMongoRepository;
import agent.tracker.service.infrastructure.mongo.IdempotencyRecordDocument;
import agent.tracker.service.infrastructure.mongo.MongoIndexManifest;
import com.mongodb.client.MongoClient;
import java.time.Instant;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Value;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.bson.Document;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@MicronautTest
@Property(name = "task.store", value = "mongo")
class MongoTaskStoreIntegrationTest {

    @Inject
    TaskCommandService commandService;

    @Inject
    TaskQueryService queryService;

    @Inject
    TaskStore taskStore;

    @Inject
    IdempotencyMongoRepository idempotencyRepository;

    @Inject
    MongoClient mongoClient;

    @Value("${mongodb.database:agent_tracker_test}")
    String databaseName;

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

    @Test
    void shouldRejectCreateReplayWhenPayloadDiffers() {
        commandService.createTask(new CreateTaskCommand(
            "same-key-original",
            "mongo",
            TaskType.FEATURE,
            TaskPriority.HIGH,
            "integration",
            "mongo-create-mismatch-1"
        ));

        assertThrows(IdempotencyKeyReuseMismatchException.class, () ->
            commandService.createTask(new CreateTaskCommand(
                "same-key-changed",
                "mongo",
                TaskType.FEATURE,
                TaskPriority.HIGH,
                "integration",
                "mongo-create-mismatch-1"
            ))
        );
    }

    @Test
    void shouldReturnNullWhenReplayReferenceTaskNoLongerExists() {
        Instant now = Instant.now();
        idempotencyRepository.save(new IdempotencyRecordDocument(
            "create_task:missing-ref-key",
            "create_task",
            "missing-ref-key",
            "payload-hash",
            "missing-task-id",
            now.plusSeconds(3600),
            now,
            now
        ));

        Task replay = taskStore.findCreateReplay("missing-ref-key", "payload-hash");

        assertNull(replay);
    }

    @Test
    void shouldSupportCursorPaginationFromMongoStorePath() {
        for (int i = 0; i < 3; i++) {
            commandService.createTask(new CreateTaskCommand(
                "mongo-page-" + i,
                "desc",
                TaskType.FEATURE,
                TaskPriority.MEDIUM,
                "integration",
                "mongo-page-key-" + i
            ));
        }

        TaskListPage first = queryService.listTasks(null, null, 2);
        assertEquals(2, first.tasks().size());
        assertNotNull(first.nextCursor());

        TaskListPage second = queryService.listTasks(null, first.nextCursor(), 2);
        assertEquals(1, second.tasks().size());
        assertNull(second.nextCursor());

        TaskListPage secondWithOffsetToken = queryService.listTasks(null, "o:" + first.nextCursor(), 2);
        assertEquals(second.tasks().stream().map(Task::getTaskId).toList(), secondWithOffsetToken.tasks().stream().map(Task::getTaskId).toList());

        TaskListPage secondWithUppercaseOffsetToken = queryService.listTasks(null, "O: " + first.nextCursor(), 2);
        assertEquals(second.tasks().stream().map(Task::getTaskId).toList(), secondWithUppercaseOffsetToken.tasks().stream().map(Task::getTaskId).toList());
    }

    @Test
    void shouldRejectUnsupportedCursorPrefixOnMongoStorePath() {
        assertThrows(IllegalArgumentException.class, () -> queryService.listTasks(null, "s:1", 20));
    }

    @Test
    void shouldProvisionRequiredMongoIndexesFromManifest() {
        var database = mongoClient.getDatabase(databaseName);

        for (var required : MongoIndexManifest.REQUIRED_INDEXES) {
            Document index = database.getCollection(required.collection())
                .listIndexes()
                .into(new java.util.ArrayList<>())
                .stream()
                .filter(candidate -> required.name().equals(candidate.getString("name")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing required index: " + required.collection() + "/" + required.name()));

            assertEquals(required.keys(), index.get("key"), "index key mismatch for " + required.name());
            assertEquals(required.unique(), Boolean.TRUE.equals(index.getBoolean("unique")), "unique option mismatch for " + required.name());

            if (required.expireAfterSeconds() == null) {
                assertNull(index.get("expireAfterSeconds"), "ttl should be absent for " + required.name());
            } else {
                Object ttl = index.get("expireAfterSeconds");
                assertNotNull(ttl, "ttl should exist for " + required.name());
                assertEquals(required.expireAfterSeconds().longValue(), ((Number) ttl).longValue(), "ttl mismatch for " + required.name());
            }
        }
    }
}
