package agent.tracker.service.docs;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReleaseReadinessDocumentationContractTest {

    private static final List<Path> ACTIVE_DOCS = List.of(
        Path.of("ARCHITECTURE.md"),
        Path.of("PRODUCT_OWNER_NEXT.md"),
        Path.of("HANDOFF_REST_MCP_MVP.md")
    );

    @Test
    void shouldReferenceOnlyCanonicalAdrFilesInActivePlanningDocs() throws Exception {
        for (Path path : ACTIVE_DOCS) {
            String text = Files.readString(path);

            assertTrue(text.contains("ADR-012-mcp-correlation-id-canonicalization-policy.md"), path + " should reference canonical ADR-012");
            assertTrue(text.contains("ADR-013-task-list-pagination-ordering-contract.md"), path + " should reference canonical ADR-013");

            assertFalse(text.contains("ADR-012-mcp-correlation-id-precedence-and-fallback.md"), path + " should not reference superseded ADR-012 variant");
            assertFalse(text.contains("ADR-013-task-list-pagination-contract-v1-offset-cursor.md"), path + " should not reference superseded ADR-013 variant");
        }
    }

    @Test
    void shouldKeepReleaseEvidenceTemplateAndPrTemplateAlignedToAdr016() throws Exception {
        String evidenceTemplate = Files.readString(Path.of("docs", "release-evidence.md"));
        String prTemplate = Files.readString(Path.of(".github", "pull_request_template.md"));

        assertTrue(evidenceTemplate.contains("JDK 21"));
        assertTrue(evidenceTemplate.contains("verifyOpenApiSnapshot"));
        assertTrue(evidenceTemplate.contains("ADR-016-release-readiness-evidence-and-go-no-go-gate.md"));

        assertTrue(prTemplate.contains("docs/release-evidence.md"));
        assertTrue(prTemplate.contains("verifyOpenApiSnapshot"));
        assertTrue(prTemplate.contains("Canonical ADR set used"));
    }
}
