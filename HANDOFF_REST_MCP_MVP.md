# Developer Handoff — REST + MCP MVP

Last updated: 2026-02-24 (PST)

## What was done in this product/architecture pass
- Re-validated code/docs parity for REST + MCP readiness.
- Rebased roadmap to current state: only three MVP blockers remain (`P0-A2`, `P0-A3`, `P0-B1`).
- Marked contract/ops items now complete in code baseline:
  - Error-code lock tests (`ErrorCatalogContractTest`) ✅
  - Idempotency observability markers + tests (`TaskCommandServiceObservabilityTest`) ✅
  - MCP code-level registration/schema contract tests (`TaskMcpToolRegistrationContractTest`) ✅
- Updated architecture + backlog docs to remove stale “not implemented” statements.

## Current engineering baseline (code reality)
- REST task endpoints are implemented and stable for core flows.
- MCP application adapter (`TaskMcpTools`) is implemented for create/get/list/update-status.
- Baseline REST/MCP parity tests exist (`TaskRestMcpParityTest`) for create, transition, mismatch semantics.
- Error contract includes stable 409 mappings for:
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

### 1) TKT-P0-A2 — Wire parity suite into CI (highest impact)
**Goal**: parity drift must fail builds.

Definition of done:
- CI executes `TaskRestMcpParityTest` on PRs.
- Any semantic divergence across adapters fails the check.
- Build output clearly identifies the failing parity scenario.

### 2) TKT-P0-A3 — MCP runtime registration/schema verification
**Goal**: validate transport-level discoverability beyond code-level reflection tests.

Definition of done:
- Runtime reports all 4 task tools discoverable.
- Required fields match current request records (`taskId`, `idempotencyKey`, etc.).
- A reproducible local smoke command sequence is documented and CI-checkable.

### 3) TKT-P0-B1 — OpenAPI snapshot + drift gate
**Goal**: contract changes become explicit review events.

Definition of done:
- Generated `openapi.yaml` is committed.
- CI fails on drift unless snapshot update is intentional in PR.
- Error examples include `CONCURRENT_MODIFICATION` and `IDEMPOTENCY_KEY_REUSE_MISMATCH`.

## Suggested immediate execution plan (next dev cycle)
1. **CI wiring first**: make `TaskRestMcpParityTest` a required check (`P0-A2`).
2. **MCP smoke second**: add runtime inventory/schema verification and document exact command (`P0-A3`).
3. **OpenAPI gate third**: add snapshot generation and diff enforcement (`P0-B1`).

## Critical regression scenarios to keep green
- Replay with same operation+key+same payload => replay success
- Replay with same operation+key+different payload => 409 mismatch
- Concurrent update race => `CONCURRENT_MODIFICATION`
- Invalid status filter => stable 400 (`BAD_REQUEST`)
- REST vs MCP scenario equivalence (state + error code)

## File map for next implementer
- Backlog + acceptance criteria: `PRODUCT_OWNER_NEXT.md`
- Architecture truth source: `ARCHITECTURE.md`
- Parity CI + test evolution: `src/test/java/.../mcp/TaskRestMcpParityTest.java`, CI workflow files
- MCP runtime verification docs/runbook: `README.md` (or dedicated runbook)
- Contract gate: OpenAPI generation config + committed `openapi.yaml`

## Key guardrails
- Keep MCP handlers thin; do not duplicate domain/business rules.
- Preserve task-only v1 surface (project APIs remain deferred).
- Treat ADR-007 posture as authoritative unless superseded by a new ADR.
