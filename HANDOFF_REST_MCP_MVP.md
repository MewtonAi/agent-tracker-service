# Developer Handoff — REST + MCP Readiness

Last updated: 2026-02-24 (PST, product/architecture continuity pass)

## What was accomplished in this pass
- Re-inspected latest code/docs/tests/CI posture for release readiness.
- Added **ADR-019** (`ADR-019-release-evidence-provenance-and-freshness-policy.md`) to lock evidence source + SHA parity + <=24h freshness policy.
- Updated release workflow artifacts:
  - `docs/release-evidence.md` (provenance/freshness checks)
  - `.github/pull_request_template.md` (canonical ADR set + freshness gate)
- Added roadmap artifact: `docs/rest-mcp-readiness-roadmap.md` with implementation-ready slice sequencing.
- Refined backlog/ticket priorities in `PRODUCT_OWNER_NEXT.md`.

## Current state to assume
1. **CI gate:** `.github/workflows/ci.yml` runs JDK 21 + `./gradlew check`.
2. **Primary blocker:** OpenAPI snapshot reconciliation still needs Java 21 execution evidence.
3. **Release lane policy:** ADR-018 feature-freeze applies until Lane 1 closure.
4. **Evidence policy:** ADR-019 requires source declaration, PR-head SHA parity, and <=24h freshness at GO decision time.

## Next coding slice (strict order)
1. **TKT-P1-G15** — run in Java 21 environment:
   - `./gradlew updateOpenApiSnapshot`
   - `./gradlew check`
   - commit `openapi/openapi.yaml` only if regenerated
2. **TKT-P1-G19** — complete release evidence + PR template fields with provenance/freshness data.
3. **TKT-P1-G17** — perform final canonical ADR reference sweep.
4. **TKT-P2-A18** — continue cursor phase-2 planning only after lane closure.

## Done criteria for immediate slice
- CI green for PR head SHA.
- `verifyOpenApiSnapshot` pass outcome captured.
- `docs/release-evidence.md` populated with:
  - source type, CI URL, commit SHA
  - parity statement + OpenAPI diff status
  - canonical ADR list used
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

## Environment caveat for this run
- Local shell still lacks Java (`JAVA_HOME` not set, `java` not found); in-session test/verification execution remains blocked.
