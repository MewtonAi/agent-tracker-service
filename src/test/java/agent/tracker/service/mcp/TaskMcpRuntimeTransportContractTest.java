package agent.tracker.service.mcp;

import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
class TaskMcpRuntimeTransportContractTest {

    @Inject
    ApplicationContext context;

    @Test
    void shouldBootWithHttpMcpTransportAndTaskToolsBean() {
        String transport = context.getProperty("micronaut.mcp.server.transport", String.class).orElse("");
        assertEquals("HTTP", transport);
        assertTrue(context.containsBean(TaskMcpTools.class));
    }

    @Test
    void shouldExposeExpectedTaskToolSignaturesAtRuntime() {
        TaskMcpTools tools = context.getBean(TaskMcpTools.class);
        Set<String> methodNames = Arrays.stream(tools.getClass().getMethods())
            .filter(method -> method.getDeclaringClass().equals(TaskMcpTools.class))
            .map(Method::getName)
            .collect(Collectors.toSet());

        assertTrue(methodNames.contains("createTask"));
        assertTrue(methodNames.contains("getTask"));
        assertTrue(methodNames.contains("listTasks"));
        assertTrue(methodNames.contains("updateTaskStatus"));
    }
}
