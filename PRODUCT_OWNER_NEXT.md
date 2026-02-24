# PRODUCT_OWNER_NEXT.md

Last updated: 2026-02-23 (PST)
Owner: Product/Architecture

## Implementation Checkpoint (code-verified)

### What is shipped now
- ✅ Canonical v1 scope is **Task-only**.
- ✅ Canonical lifecycle in code: `NEW, IN_PROGRESS, BLOCKED, DONE, CANCELED`.
- ✅ Transition policy implemented and enforced:
  - `NEW -> IN_PROGRESS | CANCELED`
  - `IN_PROGRESS -> BLOCKED | DONE | CANCELED`
  - `BLOCKED -> IN_PROGRESS | CANCELED`
  - `DONE` and `CANCELED` terminal
- ✅ REST endpoints implemented:
  - `POST /v1/tasks` (requires `Idempotency-Key`)
  - `GET /v1/tasks/{id}`
  - `GET /v1/tasks?status=`
  - `PATCH /v1/tasks/{id}/status` (requires `Idempotency-Key`)
- ✅ RFC7807-style error contract with stable `code` and `correlationId`.
- ✅ In-memory idempotency replay behavior exists for create/status-update mutations.

### What is not shipped yet (MVP blockers)
- 🟡 Durable Mongo persistence adapter (seam + adapter/repositories implemented; concurrency behavior still needs verification hardening)
- ❌ Optimistic locking with deterministic conflict semantics on concurrent writes
- 🟡 Durable idempotency record store (Mongo-backed records implemented, duplicate-key/error mapping needs hardening)
- ❌ MCP tool surface (`create_task`, `get_task`, `list_tasks`, `update_task_status`)
- ❌ Automated REST/MCP parity suite in CI
- ❌ OpenAPI generation + contract lock pipeline

---

## Prioritized backlog (REST + MCP readiness)

## P0 — MVP release gate

### EPIC P0-A — Durable Task Store (Mongo)
**Outcome:** Replace in-memory store with durable, query-ready persistence while preserving service semantics.

#### TKT-P0-A1 — Mongo task document + repository adapter
**Scope**
- Introduce Mongo `TaskDocument` and mapping layer.
- Implement persistence-backed repository used by `TaskCommandService` and `TaskQueryService`.

**Acceptance criteria**
- Task fields round-trip without loss.
- Existing REST tests remain green with Mongo-backed wiring.
- Integration tests run against test Mongo (Testcontainers or equivalent).

#### TKT-P0-A2 — Index policy for current query paths
**Scope**
- Create indexes for current and near-term list/query patterns.

**Acceptance criteria**
- Indexes defined and auto-provisioned on startup/migration:
  - `{ status: 1, updatedAt: -1 }`
  - `{ updatedAt: -1 }`
- Index strategy documented with rationale and expected query use.

#### TKT-P0-A3 — Optimistic locking
**Scope**
- Add version field and compare-and-set update behavior.

**Acceptance criteria**
- Concurrent updates to same task produce deterministic conflict (HTTP 409).
- Conflict path covered by integration tests.
- Error code stable (`CONCURRENT_MODIFICATION` or equivalent documented code).

---

### EPIC P0-B — Durable Idempotency Contract
**Outcome:** Safe retry semantics for distributed clients and adapters.

#### TKT-P0-B1 — Idempotency record model + storage
**Scope**
- Persist idempotency records keyed by operation scope.
- Enforce uniqueness and replay semantics.

**Acceptance criteria**
- Duplicate create/status mutation with same key returns original logical result.
- Records survive process restart.
- TTL/retention policy documented (recommended 24-72h, configurable).

#### TKT-P0-B2 — Validation + observability
**Scope**
- Harden request validation and logging around idempotency and correlation IDs.

**Acceptance criteria**
- Missing/blank key returns 400 with stable error code.
- Replay vs first-write clearly observable in logs/metrics.
- Correlation ID appears on all error responses.

---

### EPIC P0-C — MCP Adapter with REST Parity
**Outcome:** Agent-native interface with equivalent behavior to REST.

#### TKT-P0-C1 — Implement MVP MCP tools
Tools: `create_task`, `get_task`, `list_tasks`, `update_task_status`

**Acceptance criteria**
- Tool schemas mirror REST constraints (required fields/status validation).
- Tool handlers call shared application services only.
- Tool tests cover happy path + not-found + invalid transition conflict.

#### TKT-P0-C2 — Parity scenario suite
**Scope**
- Build cross-adapter test harness for equivalent command sequences.

**Acceptance criteria**
- Same scenarios yield equivalent terminal state and error semantics in REST and MCP.
- Parity suite runs in CI and is documented in README/hand-off.

---

### EPIC P0-D — Contract Hardening
**Outcome:** Stable external interface and controlled change management.

#### TKT-P0-D1 — OpenAPI generation + check-in
**Acceptance criteria**
- OpenAPI generated from code and committed.
- CI detects unreviewed contract diffs.

#### TKT-P0-D2 — Error catalog lock
**Acceptance criteria**
- Centralized error-code catalog documented.
- Regression tests for key codes:
  - `TASK_NOT_FOUND`
  - `INVALID_TASK_TRANSITION`
  - `VALIDATION_FAILED`/`BAD_REQUEST`
  - concurrency conflict code

---

## P1 — Post-MVP next increment
- Cursor pagination for list API/tool.
- `task_events` timeline read model.
- Assignment invariants for terminal states.
- Baseline SLO + metrics dashboard (latency/error/replay/conflict rates).

## P2 — Strategic
- Outbox/event publishing.
- Multi-tenant authz boundaries.
- Archival and retention automation.

---

## Risks and tradeoffs to track
- **High:** in-memory store today is non-durable and non-shareable across replicas.
- **High:** idempotency currently process-local; retries across restarts can duplicate mutations.
- **Medium:** parity drift risk if MCP adapters bypass shared service layer.
- **Medium:** list endpoint currently uses `TaskStatus.valueOf`; invalid status handling should be normalized.

---

## Recommended next slice (implementation order)
1. P0-A1 + P0-A2 (Mongo adapter + indexes)
2. P0-A3 + P0-B1 (locking + durable idempotency)
3. P0-C1 + P0-C2 (MCP tools + parity suite)
4. P0-D1 + P0-D2 (OpenAPI + contract/error locks)
