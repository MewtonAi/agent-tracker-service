# Developer Handoff — REST + MCP Post-MVP Hardening

Last updated: 2026-02-24 (PST, post-inspection refresh)

## What changed in this product/architecture pass
- Re-inspected latest code/docs/tests/CI state for REST + MCP readiness.
- Closed architecture decision drift by adding:
  - `ADR-012-mcp-correlation-id-precedence-and-fallback.md` (final v1 policy; supersedes ADR-011)
  - `ADR-013-task-list-pagination-contract-v1-offset-cursor.md` (current pagination contract + follow-up path)
- Re-ranked delivery slices: **OpenAPI snapshot reconciliation first**, then pagination internals optimization.

## Current engineering baseline (verified)
- REST task endpoints stable for create/get/list/update-status.
- MCP tools implemented with shared application services.
- Pagination contract already present across transports (`limit`, `cursor`, `nextCursor`).
- MCP supports caller-provided `correlationId` and UUID fallback on errors.
- CI gate executes `./gradlew check` on push/PR (JDK 21).
- Contract suites present and gating:
  - REST/MCP parity
  - MCP registration + runtime transport handshake/discoverability
  - OpenAPI strict snapshot drift
  - Error-code catalog lock

## Environment caveat from this pass
- Local `./gradlew check` could not be run here because `JAVA_HOME` is not configured.
- OpenAPI snapshot likely stale relative to implemented pagination markers; needs Java 21 regeneration run.
- No runtime code changed in this pass; updates are docs/ADR/planning only.

## Recommended next coding slice
### TKT-P1-G15 — OpenAPI snapshot reconciliation
Definition of done:
- Run `./gradlew updateOpenApiSnapshot` and `./gradlew check` on Java 21 environment.
- Commit `openapi/openapi.yaml` if regenerated output differs.
- Confirm `verifyOpenApiSnapshot` and marker assertions pass in CI.

## Follow-on slice
### TKT-P1-A16 — Store-level pagination optimization
Definition of done:
- Move list pagination from full-read-then-slice to store-level paginated path.
- Preserve external `limit/cursor/nextCursor` semantics for REST and MCP.
- Keep parity and terminal-page behavior contract tests green.

## Implementation notes
- Maintain stable error `code` values while evolving internals.
- Keep cursor externally opaque; avoid leaking offset assumptions in contract language.
- Preserve backward compatibility for callers omitting pagination params.
