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

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
class TaskControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void shouldCreateAndFetchTask() {
        TaskResponse created = client.toBlocking().retrieve(
            HttpRequest.POST("/v1/tasks", new CreateTaskRequest("proj-1", "Create API", "desc", TaskType.FEATURE, TaskPriority.HIGH, "qa")),
            TaskResponse.class
        );

        assertNotNull(created.taskId());
        assertEquals("Create API", created.title());
        assertEquals(TaskStatus.BACKLOG, created.status());

        TaskResponse fetched = client.toBlocking().retrieve(HttpRequest.GET("/v1/tasks/" + created.taskId()), TaskResponse.class);
        assertEquals(created.taskId(), fetched.taskId());
    }

    @Test
    void shouldReturnConflictForInvalidTransition() {
        TaskResponse created = client.toBlocking().retrieve(
            HttpRequest.POST("/v1/tasks", new CreateTaskRequest("proj-2", "Transition", "desc", TaskType.BUG, TaskPriority.MEDIUM, "qa")),
            TaskResponse.class
        );

        HttpClientResponseException exception = assertThrows(HttpClientResponseException.class, () ->
            client.toBlocking().exchange(
                HttpRequest.PATCH("/v1/tasks/" + created.taskId() + "/status", new UpdateTaskStatusRequest(created.taskId(), TaskStatus.DONE, "qa")),
                TaskResponse.class
            )
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }
}
