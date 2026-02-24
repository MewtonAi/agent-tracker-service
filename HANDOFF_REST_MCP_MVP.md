# Developer Handoff — REST + MCP Readiness

Last updated: 2026-02-24 (PST, product/architecture continuity)

## What was accomplished in this pass
- Implemented ADR-015 cursor evolution compatibility in `TaskQueryService`:
  - accepted `cursor` forms `<n>` and `o:<n>`
  - retained emitted `nextCursor` behavior for backward compatibility
- Extended tests for compatibility and parity:
  - `TaskQueryServiceTest` now covers prefixed cursor acceptance/rejection paths
  - `TaskRestMcpParityTest` now verifies parity when page-2 uses `o:<cursor>`
- Added and referenced `ADR-015-cursor-token-evolution-and-backward-compatibility.md`.
- Tightened superseded ADR hygiene by adding explicit historical/canonical pointer in `ADR-011-mcp-correlation-id-source-policy.md`.
- Updated planning/docs references to align with post-implementation priorities.

## Current state to assume
1. **CI gate:** `./gradlew check` on JDK 21.
2. **Primary blocker:** OpenAPI snapshot likely stale vs shipped pagination fields and should be regenerated under Java 21.
3. **Pagination internals:** store-level paging seam is already implemented (`TaskStore#listTasksPage` + Mongo `Pageable`/`Slice`).
4. **Canonical ADR set for active work:**
   - Correlation policy: `ADR-012-mcp-correlation-id-canonicalization-policy.md`
   - Pagination ordering: `ADR-013-task-list-pagination-ordering-contract.md`
   - Supersession governance: `ADR-014-contract-source-of-truth-and-supersession-policy.md`
   - Cursor evolution compatibility: `ADR-015-cursor-token-evolution-and-backward-compatibility.md`

## Next coding slice (recommended order)
1. Run Java 21 verification and snapshot reconciliation:
   - `./gradlew updateOpenApiSnapshot`
   - `./gradlew check`
   - commit `openapi/openapi.yaml` if regenerated
2. Confirm CI pass and lock G15 completion with OpenAPI snapshot parity checks.
3. Continue reliability hardening tickets (auth boundaries/outbox/retention) after release gate recovery.

## Environment caveat for this run
- No local Java runtime available in this shell (`java` and `JAVA_HOME` missing), so tests/verification were not executed in this pass.
