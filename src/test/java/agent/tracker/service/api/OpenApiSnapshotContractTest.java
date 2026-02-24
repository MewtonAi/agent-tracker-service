package agent.tracker.service.api;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
        String snapshot = Files.readString(SNAPSHOT_PATH);

        assertTrue(snapshot.contains("- in: query\n          name: cursor"));
        assertTrue(snapshot.contains("- in: query\n          name: limit"));
        assertTrue(snapshot.contains("properties:\n                  tasks:"));
        assertTrue(snapshot.contains("nextCursor:"));
        assertTrue(snapshot.contains("nullable: true"));
    }
}
