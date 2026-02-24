package agent.tracker.service.api;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ApiSurfaceHygieneContractTest {

    @Test
    void shouldKeepDeferredProjectDtosOutOfPublicApiPackage() throws Exception {
        Path apiDtoPath = Path.of("src/main/java/agent/tracker/service/api/dto");
        if (!Files.exists(apiDtoPath)) {
            return;
        }

        List<String> names = Files.list(apiDtoPath)
            .map(path -> path.getFileName().toString())
            .toList();

        assertFalse(names.stream().anyMatch(name -> name.toLowerCase().contains("project")));
    }

    @Test
    void shouldNotExposeProjectRoutesInOpenApiSnapshot() throws Exception {
        String openApi = Files.readString(Path.of("openapi/openapi.yaml"));
        assertFalse(openApi.contains("/v1/projects"));
    }
}
