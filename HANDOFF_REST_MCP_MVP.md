# Developer Handoff — REST + MCP MVP

Last updated: 2026-02-24 (PST, late)

## What was done in this product/architecture pass
- Re-validated code/docs/CI parity for REST + MCP readiness.
- Refined roadmap to current blockers: only two MVP gate items remain (`P0-A3`, `P0-B1`).
- Converted backlog items into implementation-ready tickets with concrete acceptance criteria.
- Added decision record ADR-008 to lock MVP gate definition for MCP wire handshake + strict OpenAPI drift checks.

## Current engineering baseline (code reality)
- REST task endpoints are implemented and stable for core flows.
- MCP application adapter (`TaskMcpTools`) is implemented for create/get/list/update-status.
- REST/MCP parity tests are implemented and CI-gated via `./gradlew check` (`TaskRestMcpParityTest`).
- MCP runtime test currently validates boot + tool bean/method signatures (`TaskMcpRuntimeTransportContractTest`), not transport `tools/list` wire handshake.
- OpenAPI snapshot is committed and verified for required markers (`verifyOpenApiSnapshot`), but not yet strict generated-vs-snapshot equality.
- Error contract includes stable 409 mappings:
  - `CONCURRENT_MODIFICATION`
  - `IDEMPOTENCY_KEY_REUSE_MISMATCH`
- Mongo idempotency v2 behavior is present:
  - uniqueness on `(operation,key)`
  - mismatch detection by payload hash
  - TTL index on `expiresAt` with configurable retention
- Migration posture is explicitly v2-only (ADR-007).
- Idempotency observability markers are emitted:
  - `idempotency.first_write`
  - `idempotency.replay_hit`
  - `idempotency.mismatch_reject`

## Remaining MVP release gate (ordered)

### 1) TKT-P0-A3 — MCP transport-level handshake verification (highest impact)
**Goal**: transport discoverability/schema drift must fail builds.

Definition of done:
- Runtime `tools/list` returns all 4 task tools.
- Required fields align with current request records (`taskId`, `idempotencyKey`, etc.).
- A reproducible local smoke command sequence is documented and CI-checkable.

### 2) TKT-P0-B1 — OpenAPI strict snapshot drift gate
**Goal**: structural contract changes become explicit review events.

Definition of done:
- Generated OpenAPI is compared against committed `openapi/openapi.yaml`.
- CI fails on any drift unless snapshot update is intentional in PR.
- Error examples include `CONCURRENT_MODIFICATION` and `IDEMPOTENCY_KEY_REUSE_MISMATCH`.

## Suggested immediate execution plan (next dev cycle)
1. **MCP wire gate first**: implement `tools/list` verification and add a CI-friendly smoke command (`P0-A3`).
2. **OpenAPI hardening second**: enforce strict generated-vs-checked-in diff (`P0-B1`).
3. **Post-MVP**: promote idempotency markers to metrics dashboards/alerts (`P1-O11`).

## Critical regression scenarios to keep green
- Replay with same operation+key+same payload => replay success
- Replay with same operation+key+different payload => 409 mismatch
- Concurrent update race => `CONCURRENT_MODIFICATION`
- Invalid status filter => stable 400 (`BAD_REQUEST`)
- REST vs MCP scenario equivalence (state + error code)

## File map for next implementer
- Backlog + acceptance criteria: `PRODUCT_OWNER_NEXT.md`
- Architecture truth source: `ARCHITECTURE.md`
- Decision log for MVP gate semantics: `ADR-008-mvp-gate-tightening-for-mcp-transport-and-openapi-drift.md`
- Parity CI + test evolution: `src/test/java/.../mcp/TaskRestMcpParityTest.java`, `.github/workflows/ci.yml`
- MCP runtime verification docs/runbook: `README.md` (or dedicated runbook)
- Contract gate: `build.gradle` OpenAPI tasks + committed `openapi/openapi.yaml`

## Key guardrails
- Keep MCP handlers thin; do not duplicate domain/business rules.
- Preserve task-only v1 surface (project APIs remain deferred).
- Treat ADR-007 and ADR-008 as authoritative unless superseded by new ADRs.
