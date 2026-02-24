package agent.tracker.service.mcp;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Transport-registration guardrail: lock exposed MCP tool method names + request schema essentials.
 */
class TaskMcpToolRegistrationContractTest {

    @Test
    void shouldExposeExpectedToolMethodSurface() {
        Set<String> methodNames = Arrays.stream(TaskMcpTools.class.getDeclaredMethods())
            .filter(method -> java.lang.reflect.Modifier.isPublic(method.getModifiers()))
            .map(Method::getName)
            .collect(Collectors.toSet());

        assertTrue(methodNames.contains("createTask"));
        assertTrue(methodNames.contains("getTask"));
        assertTrue(methodNames.contains("listTasks"));
        assertTrue(methodNames.contains("updateTaskStatus"));
    }

    @Test
    void shouldKeepRequestSchemaCriticalFieldsStable() {
        assertEquals(
            Set.of("title", "description", "taskType", "priority", "requestedBy", "idempotencyKey"),
            componentNames(TaskMcpTools.CreateTaskToolRequest.class)
        );
        assertEquals(Set.of("taskId"), componentNames(TaskMcpTools.GetTaskToolRequest.class));
        assertEquals(Set.of("status"), componentNames(TaskMcpTools.ListTasksToolRequest.class));
        assertEquals(
            Set.of("taskId", "status", "requestedBy", "idempotencyKey"),
            componentNames(TaskMcpTools.UpdateTaskStatusToolRequest.class)
        );
    }

    private static Set<String> componentNames(Class<?> recordType) {
        return Arrays.stream(recordType.getRecordComponents()).map(RecordComponent::getName).collect(Collectors.toSet());
    }
}
