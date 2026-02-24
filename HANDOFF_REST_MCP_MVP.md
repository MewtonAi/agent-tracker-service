# Developer Handoff — Next Execution Slice (REST + MCP MVP)

## Objective
Deliver production-safe mutation semantics and persistence baseline without changing external v1 REST shapes.

## Canonical guardrails (do not violate)
- v1 scope: Task-only.
- Lifecycle: `NEW, IN_PROGRESS, BLOCKED, DONE, CANCELED`.
- Transition policy is domain-owned and adapter-agnostic.
- REST and MCP must call shared application services.

## Priority work package (in order)

### 1) Mongo persistence adapter (P0-A1/A2)
- Introduce `TaskDocument` + mapper.
- Replace `InMemoryTaskStore` wiring with repository abstraction.
- Add indexes:
  - `{ status: 1, updatedAt: -1 }`
  - `{ updatedAt: -1 }`

**Definition of done**
- Existing API tests pass with persistence-backed implementation.
- Integration test verifies create/get/list/update-status against Mongo.

### 2) Optimistic locking + durable idempotency (P0-A3/B1/B2)
- Add `version` to persisted task.
- Add `idempotency_records` collection with unique `(operation,key)` and TTL.
- Enforce required idempotency key for mutating operations.

**Definition of done**
- Concurrent status updates produce deterministic 409 with stable code.
- Replay of same key returns original logical result after restart.
- Missing key => 400 with stable code.

### 3) MCP MVP tool adapter + parity suite (P0-C1/C2)
- Implement tools:
  - `create_task`
  - `get_task`
  - `list_tasks`
  - `update_task_status`
- Keep handlers thin; map request/response only.

**Definition of done**
- Parity tests prove REST and MCP equivalence for happy + error paths.

### 4) Contract hardening (P0-D1/D2)
- Generate/check in OpenAPI.
- Lock error catalog with regression tests.

## Suggested ticket cut for immediate execution
- T1: repository abstraction + Mongo `TaskDocument` + mapper + wiring
- T2: index bootstrap + integration tests for list/order behavior
- T3: `version` conflict implementation + tests
- T4: durable idempotency records + replay tests
- T5: MCP 4-tool adapter + parity scenarios
- T6: OpenAPI snapshot + contract diff gate

## Known edge cases to resolve explicitly
- Invalid `status` query value in REST list should map to stable 400 contract (avoid framework-default inconsistency).
- Same `(operation,key)` with different payload policy must be explicit and tested.
- Same-state transition currently treated as no-op; keep or reject, but document and lock tests.
