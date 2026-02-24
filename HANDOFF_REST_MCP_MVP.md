# Developer Handoff — REST + MCP MVP

Last updated: 2026-02-24 (PST)

## What was done in this product/architecture pass
- Re-validated docs against current codebase state (REST + Mongo behavior).
- Updated architecture baseline and risk register in `ARCHITECTURE.md`.
- Advanced ADR set:
  - `ADR-004` moved to accepted (implementation in progress) with explicit status notes.
  - Added `ADR-005` (idempotency v2 rollout strategy: dual-read compatibility, v2-write, cleanup).
- Refined backlog and implementation-ready tickets in `PRODUCT_OWNER_NEXT.md` for immediate execution.

## Current engineering baseline (code reality)
- REST task endpoints are implemented and stable for core flows.
- Concurrency conflict contract is stabilized at `CONCURRENT_MODIFICATION`.
- Invalid list status input now resolves to contract-level 400 (`BAD_REQUEST`).
- Mongo idempotency v2 baseline is implemented (`operation`, `key`, `payloadHash`, `resultRef`, `expiresAt`) with matching indexes.
- Mismatch contract `IDEMPOTENCY_KEY_REUSE_MISMATCH` is implemented and tested.
- MCP tooling is not yet implemented.

## Recommended next coding slice (do next)

### 1) Deliver MCP + parity
- Implement tools: `create_task`, `get_task`, `list_tasks`, `update_task_status`.
- Add parity suite (REST vs MCP scenario equivalence) in CI.

### 2) OpenAPI governance
- Generate and commit `openapi.yaml`.
- Add CI drift gate against checked-in snapshot.

### 3) Idempotency observability + migration hardening
- Emit structured telemetry for `first_write`, `replay_hit`, `mismatch_reject`.
- Decide whether legacy-key dual-read migration is required and time-box cleanup.

## Suggested ticket cut
- **T1:** idempotency v2 schema/index migration + compatibility path
- **T2:** mismatch reject logic + exception mapping + tests
- **T3:** structured idempotency telemetry counters/logs
- **T4:** MCP tool handlers (thin transport adapters)
- **T5:** parity suite and CI integration
- **T6:** OpenAPI snapshot + drift gate

## Critical regression scenarios
- replay: same operation+key+same payload (expect replay)
- replay: same operation+key+different payload (expect 409 mismatch)
- concurrent status updates on same task (expect stable conflict code)
- invalid status filter input (expect stable 400 shape)
- replay behavior across restart with mongo-backed storage

## Notes for implementer
- Keep fallback logic explicitly feature-flagged/time-bounded to avoid permanent legacy complexity.
- Do not implement MCP-specific business rules; reuse application service methods only.
