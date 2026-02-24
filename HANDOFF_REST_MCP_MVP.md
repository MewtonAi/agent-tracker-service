# Developer Handoff — REST + MCP Readiness

Last updated: 2026-02-24 (PST, product/architecture continuity + ADR-020 docs-contract pass)

## What was accomplished in this pass
- Re-inspected latest code/docs/ADRs/tests/CI posture for release readiness.
- Added **ADR-020** (`ADR-020-release-contract-documentation-coverage-policy.md`) to formalize docs contract-test coverage for the canonical release ADR set.
- Expanded `ReleaseReadinessDocumentationContractTest` to enforce:
  - canonical ADR set coverage through ADR-020
  - planning artifact alignment (`PRODUCT_OWNER_NEXT.md`, `docs/rest-mcp-readiness-roadmap.md`)
  - ADR-019 provenance/freshness assertions in evidence + PR templates.
- Refined backlog and execution sequencing in `PRODUCT_OWNER_NEXT.md` and `docs/rest-mcp-readiness-roadmap.md`.

## Current state to assume
1. **CI gate:** `.github/workflows/ci.yml` runs JDK 21 + `./gradlew check`.
2. **Primary blocker:** OpenAPI snapshot reconciliation still needs Java 21 execution evidence.
3. **Release lane policy:** ADR-018 feature-freeze applies until Lane 1 closure.
4. **Evidence policy:** ADR-019 requires source declaration, PR-head SHA parity, and <=24h freshness at GO decision time.
5. **Docs governance policy:** ADR-020 requires same-PR test/doc updates for release-policy ADR additions.

## Next coding slice (strict order)
1. **TKT-P1-G15** — run in Java 21 environment:
   - `./gradlew updateOpenApiSnapshot`
   - `./gradlew check`
   - commit `openapi/openapi.yaml` only if regenerated
2. **TKT-P1-G19** — complete release evidence + PR template fields with provenance/freshness data.
3. **TKT-P1-G17** — final canonical ADR reference sweep + stale wording cleanup.
4. **TKT-P2-A18** — continue cursor phase-2 planning only after lane closure.

## Done criteria for immediate slice
- CI green for PR head SHA.
- `verifyOpenApiSnapshot` pass outcome captured.
- `docs/release-evidence.md` populated with:
  - source type, CI URL, commit SHA
  - parity statement + OpenAPI diff status
  - canonical ADR list used (through ADR-020)
  - GO/NO-GO decision with owner/timestamp
  - freshness validation (<=24h)

## Canonical ADR set for active release work
- `ADR-012-mcp-correlation-id-canonicalization-policy.md`
- `ADR-013-task-list-pagination-ordering-contract.md`
- `ADR-014-contract-source-of-truth-and-supersession-policy.md`
- `ADR-015-cursor-token-evolution-and-backward-compatibility.md`
- `ADR-016-release-readiness-evidence-and-go-no-go-gate.md`
- `ADR-017-release-evidence-artifact-and-pr-template-policy.md`
- `ADR-018-release-candidate-readiness-lanes-and-feature-freeze-policy.md`
- `ADR-019-release-evidence-provenance-and-freshness-policy.md`
- `ADR-020-release-contract-documentation-coverage-policy.md`

## Environment caveat for this run
- Local shell still lacks Java (`JAVA_HOME` not set, `java` not found); in-session test/verification execution remains blocked.
