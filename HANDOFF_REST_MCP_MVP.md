# Developer Handoff — REST + MCP MVP (Updated)

Last updated: 2026-02-23 (PST)

## What changed in product/architecture docs this run
- Re-baselined architecture and backlog against current code (not prior assumptions).
- Confirmed shipped vs missing for REST + Mongo + MCP parity.
- Converted backlog into implementation-ready tickets with acceptance criteria.
- Highlighted contract drifts that should be resolved before MCP rollout.

## Current baseline (code reality)
- REST v1 task endpoints are active and domain lifecycle is enforced.
- Mongo adapter exists and supports optimistic locking via `@Version`.
- In-memory remains default unless `task.store=mongo` is set.
- MCP dependency exists but no tool handlers are implemented yet.

## Highest-priority next coding slice (do this next)

### 1) Harden external mutation contract
1. Lock a single concurrency conflict code (recommend `CONCURRENT_MODIFICATION`).
2. Normalize invalid status filter behavior to stable 400 error response.
3. Define/idempotency mismatch policy for same key with different payload.

### 2) Complete durable idempotency
1. Evolve record schema to include `operation`, `payloadHash`, `resultRef`, `expiresAt`.
2. Add unique index on `(operation, key)` + TTL index on `expiresAt`.
3. Add integration tests for replay and mismatch rejection.

### 3) Deliver MCP parity
1. Implement tools: `create_task`, `get_task`, `list_tasks`, `update_task_status`.
2. Build parity test harness to execute equivalent REST and MCP scenarios.

## Suggested concrete ticket cut (engineering)
- **T1:** error-code lock + tests + doc sync
- **T2:** safe status parser/validator + controller tests
- **T3:** idempotency schema v2 + migration/index bootstrap updates
- **T4:** replay/mismatch integration tests + observability logs
- **T5:** MCP tool handlers (thin adapter only)
- **T6:** parity test suite in CI
- **T7:** OpenAPI snapshot + diff gate

## Critical edge cases to explicitly test
- `GET /v1/tasks?status=INVALID`
- retry same idempotency key + same payload
- retry same idempotency key + different payload
- concurrent status updates on same task
- replay behavior across service restart (mongo-backed)

## Developer note
Before implementing MCP tools, finish T1–T4 first so MCP inherits stable mutation semantics and error vocabulary from the start.
