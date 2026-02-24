package agent.tracker.service.mcp;

import agent.tracker.service.api.dto.CreateTaskRequest;
import agent.tracker.service.api.dto.TaskResponse;
import agent.tracker.service.api.dto.UpdateTaskStatusRequest;
import agent.tracker.service.domain.model.TaskPriority;
import agent.tracker.service.domain.model.TaskStatus;
import agent.tracker.service.domain.model.TaskType;
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
            "parity-mcp-create-1"
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
            "parity-mcp-mismatch-1"
        ));

        McpToolException mcpException = assertThrows(McpToolException.class, () ->
            mcpTools.createTask(new TaskMcpTools.CreateTaskToolRequest(
                "Mismatch changed",
                "desc",
                TaskType.FEATURE,
                TaskPriority.HIGH,
                "qa",
                "parity-mcp-mismatch-1"
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
            "parity-mcp-transition-create"
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
            "parity-mcp-transition-update"
        ));

        assertEquals(restUpdated.status(), mcpUpdated.status());
        assertEquals(TaskStatus.IN_PROGRESS, mcpUpdated.status());

        TaskMcpTools.ListTasksToolResponse mcpList = mcpTools.listTasks(new TaskMcpTools.ListTasksToolRequest("in_progress"));
        assertTrue(mcpList.tasks().stream().anyMatch(task -> task.taskId().equals(mcpUpdated.taskId())));
    }
}
