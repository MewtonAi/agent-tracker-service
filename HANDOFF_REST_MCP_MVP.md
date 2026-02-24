# Developer Handoff — REST + MCP Post-MVP Hardening

Last updated: 2026-02-24 (PST, post-inspection refresh)

## What changed in this product/architecture pass
- Re-inspected latest code/docs/tests/CI state for REST + MCP readiness.
- Re-ranked next slices: **pagination contract first**, then MCP caller-correlation propagation semantics.
- Added decision record:
  - `ADR-011-mcp-correlation-id-source-policy.md`
  - Interim policy: MCP guarantees non-empty `correlationId` on failures, using server-generated source until explicit propagation precedence is designed.
- Updated planning/architecture docs to align with current implementation reality.

## Current engineering baseline (verified)
- REST task endpoints stable for create/get/list/update-status.
- MCP tools implemented with shared application services.
- CI gate executes `./gradlew check` on push/PR (JDK 21).
- Contract suites present and gating:
  - REST/MCP parity
  - MCP registration + runtime transport handshake/discoverability
  - OpenAPI strict snapshot drift
  - Error-code catalog lock
- Idempotency counters and threshold guidance are documented and test-covered.

## Environment caveat from this pass
- Local `./gradlew check` could not be run here because `JAVA_HOME` is not configured.
- No runtime code was changed in this pass; updates are docs/ADR/planning only.

## Recommended next coding slice
### TKT-P1-A13 — Task list cursor pagination contract (REST + MCP parity)
Definition of done:
- REST list route supports bounded `limit` + `cursor` and returns deterministic `nextCursor`.
- MCP `listTasks` mirrors equivalent pagination semantics.
- Parity tests cover multi-page equivalence and terminal-page behavior.
- OpenAPI snapshot + MCP tool schema/runtime tests updated in same slice.

## Follow-on slice
### TKT-P1-O12b — MCP caller-supplied correlation propagation semantics
Definition of done:
- Explicit source precedence selected and documented (supersede ADR-011).
- MCP reuses caller correlation token when present via chosen source; generates fallback otherwise.
- Error-path contract tests cover propagation + fallback.

## Implementation notes
- Preserve stable error `code` values while evolving payload shape/metadata behavior.
- Keep contract tests as first-class gate to avoid transport drift.
- Maintain backward compatibility for list callers that omit pagination params.

## Update 2026-02-24 (developer subagent)
- Added cursor pagination contract across transports (REST list envelope + MCP list request/response pagination fields).
- Added MCP correlation-id propagation contract: request-provided token reused in errors; UUID fallback retained.
- Expanded parity/contract tests accordingly.
- Pending on maintainer machine: run ./gradlew updateOpenApiSnapshot check with Java 21 and commit regenerated openapi/openapi.yaml if drifted.

