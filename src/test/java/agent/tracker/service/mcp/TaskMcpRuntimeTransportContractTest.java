package agent.tracker.service.mcp;

import io.micronaut.context.ApplicationContext;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.web.router.Router;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
class TaskMcpRuntimeTransportContractTest {

    @Inject
    ApplicationContext context;

    @Inject
    Router router;

    @Inject
    @Client("/")
    HttpClient httpClient;

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

    @Test
    void shouldServeMcpToolsListOverHttpTransport() {
        String mcpHttpPath = resolveMcpHttpPath();
        assertFalse(mcpHttpPath.isBlank(), "MCP HTTP route was not resolved");

        // MCP wire handshake baseline: initialize then tools/list over HTTP transport.
        var initializeResponse = exchangeJsonRpc(mcpHttpPath, Map.of(
            "jsonrpc", "2.0",
            "id", "init-1",
            "method", "initialize",
            "params", Map.of(
                "protocolVersion", "2024-11-05",
                "capabilities", Map.of(),
                "clientInfo", Map.of("name", "contract-test", "version", "1.0.0")
            )
        ), Optional.empty());

        Optional<String> sessionId = Optional.ofNullable(initializeResponse.getHeaders().get("mcp-session-id"));

        var toolsListResponse = exchangeJsonRpc(mcpHttpPath, Map.of(
            "jsonrpc", "2.0",
            "id", "tools-list-1",
            "method", "tools/list",
            "params", Map.of()
        ), sessionId);

        String body = toolsListResponse.body();
        assertNotNull(body);
        assertTrue(body.contains("jsonrpc"));
        assertTrue(body.contains("createTask"));
        assertTrue(body.contains("getTask"));
        assertTrue(body.contains("listTasks"));
        assertTrue(body.contains("updateTaskStatus"));
        assertTrue(body.contains("idempotencyKey"));
        assertTrue(body.contains("taskId"));
        assertTrue(body.contains("correlationId"));
        assertTrue(body.contains("limit"));
        assertTrue(body.contains("cursor"));
    }

    private io.micronaut.http.HttpResponse<String> exchangeJsonRpc(String path, Map<String, Object> payload, Optional<String> sessionId) {
        MutableHttpRequest<Map<String, Object>> request = HttpRequest.POST(path, payload)
            .contentType(MediaType.APPLICATION_JSON_TYPE)
            .accept(MediaType.APPLICATION_JSON_TYPE);
        sessionId.ifPresent(id -> request.header("mcp-session-id", id));
        return httpClient.toBlocking().exchange(request, String.class);
    }

    private String resolveMcpHttpPath() {
        return router.uriRoutes().stream()
            .filter(route -> route.getHttpMethodName().equals("POST"))
            .map(route -> route.getUriMatchTemplate().toPathString())
            .filter(path -> path.toLowerCase().contains("mcp"))
            .findFirst()
            .orElse("");
    }
}
