# Developer Handoff — REST + MCP MVP

Last updated: 2026-02-24 (PST)

## What was done in this product/architecture pass
- Re-validated code/docs parity for REST + MCP readiness.
- Added **ADR-007** to lock migration posture: **idempotency is v2-only** (no legacy key-only fallback in this repo lineage).
- Marked ADR-005 as superseded to avoid roadmap ambiguity.
- Updated architecture + product backlog to reflect current truth and remaining MVP gates.

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
- Migration posture is now explicitly documented as v2-only (ADR-007).

## Remaining MVP release gate (ordered)

### 1) TKT-P0-A2 — Wire parity suite into CI (highest impact)
**Goal**: parity drift must fail builds.

Suggested implementation notes:
- Ensure parity test class is in the default test task path.
- Add CI step that executes parity tests on PRs.
- Treat any REST/MCP semantic mismatch as blocking.

### 2) TKT-P0-A3 — MCP runtime registration/schema verification
**Goal**: validate transport-level discoverability beyond plain unit/integration invocation.

Suggested implementation notes:
- Run service in MCP-capable mode and inspect discovered tools.
- Verify all 4 tools are present and required params are enforced.
- Capture command + expected output in README or a dedicated runbook snippet.

### 3) TKT-P0-B1 — OpenAPI snapshot + drift gate
**Goal**: contract changes become explicit review events.

Suggested implementation notes:
- Generate and commit `openapi.yaml`.
- Add CI check to fail on drift unless snapshot is intentionally updated.
- Ensure error examples include `CONCURRENT_MODIFICATION` and `IDEMPOTENCY_KEY_REUSE_MISMATCH`.

### 4) TKT-P0-B2 — Error code lock tests
**Goal**: prevent accidental renames of externally visible `code` values.

Suggested implementation notes:
- Add focused tests over representative REST failure paths.
- Lock currently documented code set.

### 5) TKT-P0-C1 — Idempotency replay observability
**Goal**: operations can measure replay quality and key misuse.

Suggested implementation notes:
- Emit counters/events:
  - `idempotency.first_write`
  - `idempotency.replay_hit`
  - `idempotency.mismatch_reject`
- Include `operation` dimension.

## Critical regression scenarios to keep green
- Replay with same operation+key+same payload => replay success
- Replay with same operation+key+different payload => 409 mismatch
- Concurrent update race => `CONCURRENT_MODIFICATION`
- Invalid status filter => stable 400 (`BAD_REQUEST`)
- REST vs MCP scenario equivalence (state + error code)

## File map for next implementer
- Parity CI + test evolution: `src/test/java/.../mcp/TaskRestMcpParityTest.java`, CI workflow files
- MCP runtime verification docs: `README.md` (or dedicated runbook)
- Contract gate: OpenAPI generation config + committed `openapi.yaml`
- Error code lock tests: `src/test/java/.../api/*`
- Observability: `TaskCommandService`, `MongoTaskStore` (and any metrics abstraction added)

## Key guardrails
- Keep MCP handlers thin; do not duplicate domain/business rules.
- Preserve task-only v1 surface (project APIs remain deferred).
- Treat ADR-007 posture as authoritative unless a new ADR says otherwise.
