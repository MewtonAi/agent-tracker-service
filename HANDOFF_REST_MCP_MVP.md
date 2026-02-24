# Developer Handoff — REST + MCP MVP

Last updated: 2026-02-24 (PST)

## What was done in this product/architecture pass
- Re-verified repo state vs architecture/docs after latest idempotency work.
- Updated ADR-004 to reflect implemented conflict + mismatch contract decisions.
- Refined architecture and backlog docs to shift priority from idempotency build-out to MCP + parity + contract governance.
- Produced implementation-ready tickets with acceptance criteria for the next delivery slices.

## Current engineering baseline (code reality)
- REST task endpoints are implemented and stable for core flows.
- Error contract includes stable 409 mappings for:
  - `CONCURRENT_MODIFICATION`
  - `IDEMPOTENCY_KEY_REUSE_MISMATCH`
- Mongo idempotency v2 behavior is present:
  - uniqueness on `(operation,key)`
  - mismatch detection by payload hash
  - TTL index on `expiresAt` with configurable retention
- MCP tooling/parity/governance are the main remaining MVP gaps.

## Recommended next coding slice (do next)

### 1) Deliver MCP + parity (highest priority)
- Implement MCP tools: `create_task`, `get_task`, `list_tasks`, `update_task_status`.
- Add parity suite that runs same scenarios through REST and MCP.

### 2) Add contract governance
- Generate + commit `openapi.yaml` snapshot.
- Add CI drift gate.
- Add error code lock tests.

### 3) Add idempotency observability and migration decision record
- Emit `idempotency.first_write`, `idempotency.replay_hit`, `idempotency.mismatch_reject`.
- Record explicit decision on legacy fallback posture (required vs disallowed).

## Suggested ticket cut
- **T1:** MCP tool handlers (thin adapters to existing services)
- **T2:** REST/MCP parity harness + CI job
- **T3:** OpenAPI snapshot + drift gate
- **T4:** Error catalog lock tests
- **T5:** Idempotency replay telemetry
- **T6:** Migration posture ADR/note + enforcement tests

## Critical regression scenarios
- replay: same operation+key+same payload (expect replay)
- replay: same operation+key+different payload (expect 409 mismatch)
- concurrent status updates on same task (expect stable conflict code)
- invalid status filter input (expect stable 400 shape)
- parity scenario equivalence between REST and MCP adapters

## Notes for implementer
- Do not add MCP-specific business rules; reuse application services only.
- Keep public docs/contracts explicit that v1 is task-only.
- If legacy fallback is intentionally unsupported, lock that with tests/docs so it does not creep back in.
