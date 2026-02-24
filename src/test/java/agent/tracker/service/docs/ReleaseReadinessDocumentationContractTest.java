package agent.tracker.service.docs;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReleaseReadinessDocumentationContractTest {

    private static final List<Path> FILENAME_REFERENCING_DOCS = List.of(
        Path.of("ARCHITECTURE.md"),
        Path.of("HANDOFF_REST_MCP_MVP.md")
    );

    private static final List<String> CANONICAL_ADRS = List.of(
        "ADR-012-mcp-correlation-id-canonicalization-policy.md",
        "ADR-013-task-list-pagination-ordering-contract.md",
        "ADR-014-contract-source-of-truth-and-supersession-policy.md",
        "ADR-015-cursor-token-evolution-and-backward-compatibility.md",
        "ADR-016-release-readiness-evidence-and-go-no-go-gate.md",
        "ADR-017-release-evidence-artifact-and-pr-template-policy.md",
        "ADR-018-release-candidate-readiness-lanes-and-feature-freeze-policy.md",
        "ADR-019-release-evidence-provenance-and-freshness-policy.md",
        "ADR-020-release-contract-documentation-coverage-policy.md",
        "ADR-021-java21-toolchain-readiness-and-verification-provenance-ladder.md"
    );

    private static final List<String> SUPERSEDED_ADRS = List.of(
        "ADR-012-mcp-correlation-id-precedence-and-fallback.md",
        "ADR-013-task-list-pagination-contract-v1-offset-cursor.md"
    );

    @Test
    void shouldReferenceOnlyCanonicalAdrFilesInArchitectureAndHandoffDocs() throws Exception {
        for (Path path : FILENAME_REFERENCING_DOCS) {
            String text = Files.readString(path);

            for (String canonicalAdr : CANONICAL_ADRS) {
                assertTrue(text.contains(canonicalAdr), path + " should reference canonical ADR " + canonicalAdr);
            }

            for (String supersededAdr : SUPERSEDED_ADRS) {
                assertFalse(text.contains(supersededAdr), path + " should not reference superseded ADR " + supersededAdr);
            }
        }
    }

    @Test
    void shouldKeepPlanningNotesAlignedToCanonicalAdrSet() throws Exception {
        String readme = Files.readString(Path.of("README.md"));
        String productBacklog = Files.readString(Path.of("PRODUCT_OWNER_NEXT.md"));
        String roadmap = Files.readString(Path.of("docs", "rest-mcp-readiness-roadmap.md"));

        assertTrue(readme.contains("ADR-014"));
        assertTrue(readme.contains("ADR-015"));
        assertTrue(readme.contains("ADR-016"));
        assertTrue(readme.contains("ADR-017"));
        assertTrue(readme.contains("ADR-018"));
        assertTrue(readme.contains("ADR-019"));
        assertTrue(readme.contains("ADR-020"));
        assertTrue(readme.contains("ADR-021"));

        assertTrue(productBacklog.contains("ADR-012/013/014/015/016/017/018/019/020/021"));
        assertTrue(roadmap.contains("ADR-018"));
        assertTrue(roadmap.contains("ADR-019"));
        assertTrue(roadmap.contains("ADR-020"));
        assertTrue(roadmap.contains("ADR-021"));

        assertFalse(productBacklog.contains("ADR-012-mcp-correlation-id-precedence-and-fallback.md"));
        assertFalse(productBacklog.contains("ADR-013-task-list-pagination-contract-v1-offset-cursor.md"));
    }

    @Test
    void shouldKeepReleaseEvidenceTemplateAndPrTemplateAlignedToReleasePolicies() throws Exception {
        String evidenceTemplate = Files.readString(Path.of("docs", "release-evidence.md"));
        String prTemplate = Files.readString(Path.of(".github", "pull_request_template.md"));

        assertTrue(evidenceTemplate.contains("JDK 21"));
        assertTrue(evidenceTemplate.contains("verifyOpenApiSnapshot"));
        assertTrue(evidenceTemplate.contains("ADR-016-release-readiness-evidence-and-go-no-go-gate.md"));
        assertTrue(evidenceTemplate.contains("ADR-017-release-evidence-artifact-and-pr-template-policy.md"));
        assertTrue(evidenceTemplate.contains("ADR-019-release-evidence-provenance-and-freshness-policy.md"));
        assertTrue(evidenceTemplate.contains("ADR-020-release-contract-documentation-coverage-policy.md"));
        assertTrue(evidenceTemplate.contains("ADR-021-java21-toolchain-readiness-and-verification-provenance-ladder.md"));
        assertTrue(evidenceTemplate.contains("java -version"));
        assertTrue(evidenceTemplate.contains("JAVA_HOME"));
        assertTrue(evidenceTemplate.contains("<=24h"));

        assertTrue(prTemplate.contains("docs/release-evidence.md"));
        assertTrue(prTemplate.contains("verifyOpenApiSnapshot"));
        assertTrue(prTemplate.contains("Canonical ADR set used"));
        assertTrue(prTemplate.contains("ADR-018"));
        assertTrue(prTemplate.contains("ADR-019"));
        assertTrue(prTemplate.contains("ADR-020"));
        assertTrue(prTemplate.contains("ADR-021"));
        assertTrue(prTemplate.contains("java -version"));
        assertTrue(prTemplate.contains("JAVA_HOME"));
        assertTrue(prTemplate.contains("<=24h"));
    }

    @Test
    void shouldTrackCursorPhase2PlanInCanonicalArtifact() throws Exception {
        String phase2Plan = Files.readString(Path.of("docs", "cursor-evolution-phase2-plan.md"));

        assertTrue(phase2Plan.contains("Migration phases"));
        assertTrue(phase2Plan.contains("Rollback posture"));
        assertTrue(phase2Plan.contains("Mixed-token REST/MCP parity test plan"));
        assertTrue(phase2Plan.contains("Terminal-page semantics"));
        assertTrue(phase2Plan.contains("Malformed token handling"));
    }
}
