package agent.tracker.service.mcp;

import agent.tracker.service.api.dto.CreateTaskRequest;
import agent.tracker.service.api.dto.ListTasksResponse;
import agent.tracker.service.api.dto.TaskResponse;
import agent.tracker.service.api.dto.UpdateTaskStatusRequest;
import agent.tracker.service.domain.model.TaskPriority;
import agent.tracker.service.domain.model.TaskStatus;
import agent.tracker.service.domain.model.TaskType;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
class TaskRestMcpParityTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    TaskMcpTools mcpTools;

    @Test
    void shouldCreateEquivalentTaskViaRestAndMcp() {
        TaskResponse restCreated = client.toBlocking().retrieve(
            HttpRequest.POST("/v1/tasks", new CreateTaskRequest("Parity create", "desc", TaskType.FEATURE, TaskPriority.HIGH, "qa"))
                .header("Idempotency-Key", "parity-rest-create-1"),
            TaskResponse.class
        );

        TaskMcpTools.TaskToolResponse mcpCreated = mcpTools.createTask(new TaskMcpTools.CreateTaskToolRequest(
            "Parity create",
            "desc",
            TaskType.FEATURE,
            TaskPriority.HIGH,
            "qa",
            "parity-mcp-create-1",
            null
        ));

        assertEquals(restCreated.title(), mcpCreated.title());
        assertEquals(restCreated.status(), mcpCreated.status());
        assertEquals(restCreated.taskType(), mcpCreated.taskType());
        assertEquals(restCreated.priority(), mcpCreated.priority());
    }

    @Test
    void shouldAlignMismatchErrorCodeAcrossRestAndMcp() {
        client.toBlocking().retrieve(
            HttpRequest.POST("/v1/tasks", new CreateTaskRequest("Mismatch", "desc", TaskType.FEATURE, TaskPriority.HIGH, "qa"))
                .header("Idempotency-Key", "parity-rest-mismatch-1"),
            TaskResponse.class
        );

        HttpClientResponseException restException = assertThrows(HttpClientResponseException.class, () ->
            client.toBlocking().exchange(
                HttpRequest.POST("/v1/tasks", new CreateTaskRequest("Mismatch changed", "desc", TaskType.FEATURE, TaskPriority.HIGH, "qa"))
                    .header("Idempotency-Key", "parity-rest-mismatch-1"),
                Map.class
            )
        );

        mcpTools.createTask(new TaskMcpTools.CreateTaskToolRequest(
            "Mismatch",
            "desc",
            TaskType.FEATURE,
            TaskPriority.HIGH,
            "qa",
            "parity-mcp-mismatch-1",
            null
        ));

        McpToolException mcpException = assertThrows(McpToolException.class, () ->
            mcpTools.createTask(new TaskMcpTools.CreateTaskToolRequest(
                "Mismatch changed",
                "desc",
                TaskType.FEATURE,
                TaskPriority.HIGH,
                "qa",
                "parity-mcp-mismatch-1",
                null
            ))
        );

        assertEquals(HttpStatus.CONFLICT, restException.getStatus());
        Map<?, ?> restBody = restException.getResponse().getBody(Map.class).orElseThrow();
        assertEquals(restBody.get("code"), mcpException.getCode());
    }

    @Test
    void shouldAlignFinalStateForStatusTransition() {
        TaskResponse restCreated = client.toBlocking().retrieve(
            HttpRequest.POST("/v1/tasks", new CreateTaskRequest("Parity transition", "desc", TaskType.BUG, TaskPriority.MEDIUM, "qa"))
                .header("Idempotency-Key", "parity-rest-transition-create"),
            TaskResponse.class
        );

        TaskMcpTools.TaskToolResponse mcpCreated = mcpTools.createTask(new TaskMcpTools.CreateTaskToolRequest(
            "Parity transition",
            "desc",
            TaskType.BUG,
            TaskPriority.MEDIUM,
            "qa",
            "parity-mcp-transition-create",
            null
        ));

        TaskResponse restUpdated = client.toBlocking().retrieve(
            HttpRequest.PATCH("/v1/tasks/" + restCreated.taskId() + "/status", new UpdateTaskStatusRequest(TaskStatus.IN_PROGRESS, "qa"))
                .header("Idempotency-Key", "parity-rest-transition-update"),
            TaskResponse.class
        );

        TaskMcpTools.TaskToolResponse mcpUpdated = mcpTools.updateTaskStatus(new TaskMcpTools.UpdateTaskStatusToolRequest(
            mcpCreated.taskId(),
            TaskStatus.IN_PROGRESS,
            "qa",
            "parity-mcp-transition-update",
            null
        ));

        assertEquals(restUpdated.status(), mcpUpdated.status());
        assertEquals(TaskStatus.IN_PROGRESS, mcpUpdated.status());

        TaskMcpTools.ListTasksToolResponse mcpList = mcpTools.listTasks(new TaskMcpTools.ListTasksToolRequest("in_progress", null, null, null));
        assertTrue(mcpList.tasks().stream().anyMatch(task -> task.taskId().equals(mcpUpdated.taskId())));
    }

    @Test
    void shouldAttachCorrelationIdAcrossMcpErrorPaths() {
        McpToolException badRequest = assertThrows(McpToolException.class, () -> mcpTools.getTask(null));
        assertEquals("BAD_REQUEST", badRequest.getCode());
        assertNotNull(badRequest.getCorrelationId());
        assertFalse(badRequest.getCorrelationId().isBlank());

        String requestedCorrelationId = "corr-mcp-not-found-1";
        McpToolException notFound = assertThrows(McpToolException.class, () ->
            mcpTools.getTask(new TaskMcpTools.GetTaskToolRequest("missing-correlation", requestedCorrelationId))
        );
        assertEquals("TASK_NOT_FOUND", notFound.getCode());
        assertEquals(requestedCorrelationId, notFound.getCorrelationId());

        mcpTools.createTask(new TaskMcpTools.CreateTaskToolRequest(
            "Correlation mismatch",
            "desc",
            TaskType.FEATURE,
            TaskPriority.HIGH,
            "qa",
            "parity-mcp-correlation-mismatch-1",
            null
        ));
        McpToolException mismatch = assertThrows(McpToolException.class, () ->
            mcpTools.createTask(new TaskMcpTools.CreateTaskToolRequest(
                "Correlation mismatch changed",
                "desc",
                TaskType.FEATURE,
                TaskPriority.HIGH,
                "qa",
                "parity-mcp-correlation-mismatch-1",
                "corr-mcp-mismatch-1"
            ))
        );
        assertEquals("IDEMPOTENCY_KEY_REUSE_MISMATCH", mismatch.getCode());
        assertEquals("corr-mcp-mismatch-1", mismatch.getCorrelationId());
    }

    @Test
    void shouldAlignPaginationAcrossRestAndMcp() {
        for (int i = 0; i < 3; i++) {
            client.toBlocking().retrieve(
                HttpRequest.POST("/v1/tasks", new CreateTaskRequest("Page parity " + i, "desc", TaskType.FEATURE, TaskPriority.MEDIUM, "qa"))
                    .header("Idempotency-Key", "parity-rest-page-" + i),
                TaskResponse.class
            );
        }

        ListTasksResponse restPage1 = client.toBlocking().retrieve(HttpRequest.GET("/v1/tasks?limit=2"), ListTasksResponse.class);
        assertEquals(2, restPage1.tasks().size());
        assertNotNull(restPage1.nextCursor());

        ListTasksResponse restPage2 = client.toBlocking().retrieve(HttpRequest.GET("/v1/tasks?limit=2&cursor=" + restPage1.nextCursor()), ListTasksResponse.class);
        assertTrue(restPage2.tasks().size() >= 1);

        TaskMcpTools.ListTasksToolResponse mcpPage1 = mcpTools.listTasks(new TaskMcpTools.ListTasksToolRequest(null, null, 2, null));
        assertEquals(restPage1.tasks().size(), mcpPage1.tasks().size());
        assertEquals(restPage1.nextCursor(), mcpPage1.nextCursor());

        TaskMcpTools.ListTasksToolResponse mcpPage2 = mcpTools.listTasks(new TaskMcpTools.ListTasksToolRequest(null, mcpPage1.nextCursor(), 2, null));
        assertEquals(restPage2.tasks().size(), mcpPage2.tasks().size());
    }
}
