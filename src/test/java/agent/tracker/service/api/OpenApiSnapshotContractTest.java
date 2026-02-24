package agent.tracker.service.api;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenApiSnapshotContractTest {

    private static final Path SNAPSHOT_PATH = Path.of("openapi", "openapi.yaml");

    @Test
    void shouldContainTaskRoutesAndConflictExamples() throws IOException {
        String snapshot = Files.readString(SNAPSHOT_PATH);

        assertTrue(snapshot.contains("/v1/tasks:"));
        assertTrue(snapshot.contains("/v1/tasks/{taskId}:"));
        assertTrue(snapshot.contains("/v1/tasks/{taskId}/status:"));
        assertTrue(snapshot.contains("CONCURRENT_MODIFICATION"));
        assertTrue(snapshot.contains("IDEMPOTENCY_KEY_REUSE_MISMATCH"));
    }

    @Test
    void shouldContainListPaginationContractFields() throws IOException {
        Map<String, Object> spec = loadOpenApiSpec();

        Map<String, Object> paths = map(spec, "paths");
        Map<String, Object> listTasksPath = map(paths, "/v1/tasks");
        Map<String, Object> get = map(listTasksPath, "get");

        List<Map<String, Object>> parameters = listOfMaps(get, "parameters");
        assertTrue(parameters.stream().anyMatch(param ->
            "query".equals(param.get("in")) &&
                "cursor".equals(param.get("name")) &&
                "string".equals(map(param, "schema").get("type"))));
        assertTrue(parameters.stream().anyMatch(param ->
            "query".equals(param.get("in")) &&
                "limit".equals(param.get("name")) &&
                "integer".equals(map(param, "schema").get("type"))));

        Map<String, Object> responses = map(get, "responses");
        Map<String, Object> ok = map(responses, "200");
        Map<String, Object> content = map(ok, "content");
        Map<String, Object> json = map(content, "application/json");
        Map<String, Object> schema = map(json, "schema");
        Map<String, Object> properties = map(schema, "properties");

        assertNotNull(properties.get("tasks"));

        Map<String, Object> nextCursor = map(properties, "nextCursor");
        assertEquals("string", nextCursor.get("type"));
        assertEquals(Boolean.TRUE, nextCursor.get("nullable"));
    }

    private static Map<String, Object> loadOpenApiSpec() throws IOException {
        String snapshot = Files.readString(SNAPSHOT_PATH);
        Yaml yaml = new Yaml();
        Object loaded = yaml.load(snapshot);
        assertTrue(loaded instanceof Map, "OpenAPI snapshot must be a YAML object");
        return (Map<String, Object>) loaded;
    }

    private static Map<String, Object> map(Map<String, Object> source, String key) {
        Object value = source.get(key);
        assertTrue(value instanceof Map, "Expected map at key '" + key + "'");
        return (Map<String, Object>) value;
    }

    private static List<Map<String, Object>> listOfMaps(Map<String, Object> source, String key) {
        Object value = source.get(key);
        assertTrue(value instanceof List, "Expected list at key '" + key + "'");
        return ((List<?>) value).stream().map(entry -> {
            assertTrue(entry instanceof Map, "Expected map entries at key '" + key + "'");
            return (Map<String, Object>) entry;
        }).toList();
    }
}
