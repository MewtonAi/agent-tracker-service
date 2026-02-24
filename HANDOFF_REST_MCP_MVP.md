# Developer Handoff — REST + MCP Readiness

Last updated: 2026-02-24 (PST, release-evidence workflow pass)

## What was accomplished in this pass
- Inspected current code/docs/test/CI posture for REST + MCP readiness.
- Added **ADR-017** (`ADR-017-release-evidence-artifact-and-pr-template-policy.md`) to formalize where release evidence must live.
- Added `docs/release-evidence.md` as the canonical evidence schema.
- Added `.github/pull_request_template.md` with aligned release evidence headings.
- Updated architecture/product docs to include ADR-017 references.

## Current state to assume
1. **CI gate:** `./gradlew check` on JDK 21 (`.github/workflows/ci.yml`).
2. **Primary blocker:** OpenAPI snapshot reconciliation/verification still needs Java 21 execution.
3. **Pagination behavior:** dual-decode compatibility (`<n>` and `o:<n>`) remains supported while emitted `nextCursor` remains offset-compatible.
4. **Canonical ADR set for active work:**
   - `ADR-012-mcp-correlation-id-canonicalization-policy.md`
   - `ADR-013-task-list-pagination-ordering-contract.md`
   - `ADR-014-contract-source-of-truth-and-supersession-policy.md`
   - `ADR-015-cursor-token-evolution-and-backward-compatibility.md`
   - `ADR-016-release-readiness-evidence-and-go-no-go-gate.md`
   - `ADR-017-release-evidence-artifact-and-pr-template-policy.md`

## Next coding slice (recommended order)
1. **TKT-P1-G15:** run and commit OpenAPI reconciliation on Java 21:
   - `./gradlew updateOpenApiSnapshot`
   - `./gradlew check`
2. Complete release evidence fields in PR and handoff using `docs/release-evidence.md`.
3. **TKT-P1-G17:** run final canonical-reference sweep for any stragglers.

## Done criteria for immediate next slice
- PR shows green CI with `./gradlew check`.
- OpenAPI snapshot is verified (and committed if regenerated).
- PR + handoff both include evidence fields from `docs/release-evidence.md`:
  - CI URL + commit SHA
  - OpenAPI verification outcome and diff status
  - parity coverage statement
  - canonical ADR reference list
  - explicit GO/NO-GO statement

## Environment caveat for this run
- No Java runtime in this shell (`java`/`JAVA_HOME` unavailable), so verification execution was not possible in-session.
