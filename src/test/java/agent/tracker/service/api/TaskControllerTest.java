package agent.tracker.service.api;

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

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
class TaskControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void shouldCreateAndFetchTask() {
        TaskResponse created = client.toBlocking().retrieve(
            HttpRequest.POST("/v1/tasks", new CreateTaskRequest("Create API", "desc", TaskType.FEATURE, TaskPriority.HIGH, "qa"))
                .header("Idempotency-Key", "rest-create-1"),
            TaskResponse.class
        );

        assertNotNull(created.taskId());
        assertEquals("Create API", created.title());
        assertEquals(TaskStatus.NEW, created.status());

        TaskResponse fetched = client.toBlocking().retrieve(HttpRequest.GET("/v1/tasks/" + created.taskId()), TaskResponse.class);
        assertEquals(created.taskId(), fetched.taskId());
    }

    @Test
    void shouldReturnConflictForInvalidTransition() {
        TaskResponse created = client.toBlocking().retrieve(
            HttpRequest.POST("/v1/tasks", new CreateTaskRequest("Transition", "desc", TaskType.BUG, TaskPriority.MEDIUM, "qa"))
                .header("Idempotency-Key", "rest-create-2"),
            TaskResponse.class
        );

        HttpClientResponseException exception = assertThrows(HttpClientResponseException.class, () ->
            client.toBlocking().exchange(
                HttpRequest.PATCH("/v1/tasks/" + created.taskId() + "/status", new UpdateTaskStatusRequest(TaskStatus.DONE, "qa"))
                    .header("Idempotency-Key", "rest-status-1"),
                TaskResponse.class
            )
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    @Test
    void shouldReturnNotFoundProblemWithCorrelationId() {
        HttpClientResponseException exception = assertThrows(HttpClientResponseException.class, () ->
            client.toBlocking().exchange(
                HttpRequest.GET("/v1/tasks/missing").header("X-Correlation-Id", "corr-123"),
                Map.class
            )
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        Map<?, ?> body = exception.getResponse().getBody(Map.class).orElseThrow();
        assertEquals("TASK_NOT_FOUND", body.get("code"));
        assertEquals("corr-123", body.get("correlationId"));
    }

    @Test
    void shouldReturnBadRequestWhenIdempotencyKeyMissing() {
        HttpClientResponseException exception = assertThrows(HttpClientResponseException.class, () ->
            client.toBlocking().exchange(
                HttpRequest.POST("/v1/tasks", new CreateTaskRequest("Create API", "desc", TaskType.FEATURE, TaskPriority.HIGH, "qa")),
                Map.class
            )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void shouldAcceptCaseInsensitiveStatusFilter() {
        TaskResponse created = client.toBlocking().retrieve(
            HttpRequest.POST("/v1/tasks", new CreateTaskRequest("List API", "desc", TaskType.FEATURE, TaskPriority.HIGH, "qa"))
                .header("Idempotency-Key", "rest-create-case-filter"),
            TaskResponse.class
        );

        client.toBlocking().retrieve(
            HttpRequest.PATCH("/v1/tasks/" + created.taskId() + "/status", new UpdateTaskStatusRequest(TaskStatus.IN_PROGRESS, "qa"))
                .header("Idempotency-Key", "rest-status-case-filter"),
            TaskResponse.class
        );

        TaskResponse[] tasks = client.toBlocking().retrieve(HttpRequest.GET("/v1/tasks?status=in_progress"), TaskResponse[].class);
        assertTrue(tasks.length > 0);
    }

    @Test
    void shouldReturnBadRequestForInvalidStatusFilter() {
        HttpClientResponseException exception = assertThrows(HttpClientResponseException.class, () ->
            client.toBlocking().exchange(HttpRequest.GET("/v1/tasks?status=wat"), Map.class)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        Map<?, ?> body = exception.getResponse().getBody(Map.class).orElseThrow();
        assertEquals("BAD_REQUEST", body.get("code"));
    }

    @Test
    void shouldRejectIdempotencyKeyReuseWithDifferentPayload() {
        client.toBlocking().retrieve(
            HttpRequest.POST("/v1/tasks", new CreateTaskRequest("Original", "desc", TaskType.FEATURE, TaskPriority.HIGH, "qa"))
                .header("Idempotency-Key", "rest-mismatch-create-1"),
            TaskResponse.class
        );

        HttpClientResponseException exception = assertThrows(HttpClientResponseException.class, () ->
            client.toBlocking().exchange(
                HttpRequest.POST("/v1/tasks", new CreateTaskRequest("Different", "desc", TaskType.FEATURE, TaskPriority.HIGH, "qa"))
                    .header("Idempotency-Key", "rest-mismatch-create-1"),
                Map.class
            )
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        Map<?, ?> body = exception.getResponse().getBody(Map.class).orElseThrow();
        assertEquals("IDEMPOTENCY_KEY_REUSE_MISMATCH", body.get("code"));
    }
}
