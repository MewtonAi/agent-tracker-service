# PRODUCT_OWNER_NEXT.md

Last updated: 2026-02-23 (PST)

## Implementation Checkpoint (Dev Run 2026-02-23 20:08 PST)
- ✅ P0.1 aligned in code/docs: canonical v1 is **Task-only** with statuses `NEW, IN_PROGRESS, BLOCKED, DONE, CANCELED`.
- ✅ P0.2 core application services shipped: `TaskCommandService`, `TaskQueryService`, centralized transition policy, in-memory idempotency handling.
- ✅ P0.3 minimal REST shipped: `POST /v1/tasks`, `GET /v1/tasks/{id}`, `GET /v1/tasks`, `PATCH /v1/tasks/{id}/status` + RFC7807-style error body and correlation ID echo.
- ⏭️ Next highest priority remains P0.4 Mongo persistence adapter + indexes + optimistic locking.
Owner: Product/Architecture

## Current Delivery Snapshot (code-verified)
- ✅ Implemented: REST `/v1/tasks` endpoints for create/get/list/update-status + assign/unassign.
- ✅ Implemented: in-memory `TaskLifecycleService` with transition guard (`TaskTransitionPolicy`).
- ✅ Implemented: API exception mapping and tests for happy path + invalid transition conflict.
- ✅ Implemented: canonical task lifecycle in code: `BACKLOG -> READY -> IN_PROGRESS -> BLOCKED/IN_REVIEW -> DONE` (+ `CANCELLED`).
- ❌ Missing for MVP durability/readiness: Mongo persistence adapter, idempotency, optimistic locking, MCP tools, and REST/MCP parity tests.

---

## Canonical v1 Product Decision (authoritative)

### Scope in v1
- **In scope:** Task lifecycle management only.
- **Out of scope:** Project CRUD API (project remains a string reference on task).

### Canonical Task statuses (v1)
`BACKLOG, READY, IN_PROGRESS, BLOCKED, IN_REVIEW, DONE, CANCELLED`

### Canonical transition matrix (v1)
- `BACKLOG` -> `READY`, `CANCELLED`
- `READY` -> `BACKLOG`, `IN_PROGRESS`, `CANCELLED`
- `IN_PROGRESS` -> `READY`, `BLOCKED`, `IN_REVIEW`, `CANCELLED`
- `BLOCKED` -> `IN_PROGRESS`, `CANCELLED`
- `IN_REVIEW` -> `IN_PROGRESS`, `DONE`, `CANCELLED`
- `DONE` -> _(terminal)_
- `CANCELLED` -> _(terminal)_

Invalid transitions MUST return conflict semantics (`409` / MCP `CONFLICT`).

---

## Prioritized Backlog (REST + MCP readiness)

## P0 — Must ship for MVP

### EPIC P0-A: Durable Task Store (Mongo)
**Outcome:** Move from in-memory state to durable, concurrent-safe storage.

#### TKT-P0-A1: Mongo document + repository adapter
**Acceptance criteria**
- `Task` persists to `tasks` collection and round-trips without field loss.
- Adapter implements current `TaskLifecycleContract` behavior equivalently.
- Integration tests run against test Mongo and pass in CI.

#### TKT-P0-A2: Indexes + uniqueness policy
**Acceptance criteria**
- Indexes created for `status/priority/updatedAt` and `projectId/updatedAt` query paths.
- Unique strategy defined and enforced for caller idempotency/external key.
- Duplicate-key collisions map to domain conflict -> HTTP 409.

#### TKT-P0-A3: Optimistic locking/versioning
**Acceptance criteria**
- Persisted task includes `version`.
- Concurrent updates cause deterministic conflict behavior.
- Conflict path has integration test coverage.

---

### EPIC P0-B: Idempotent Mutation Contract
**Outcome:** Safe retries for distributed callers.

#### TKT-P0-B1: Define and enforce idempotency key contract
**Acceptance criteria**
- Mutating endpoints require `Idempotency-Key` (or approved equivalent).
- Replayed request with same key returns same logical result, no duplicate mutation.
- TTL/retention policy for idempotency records documented.

#### TKT-P0-B2: Error semantics and observability
**Acceptance criteria**
- Missing/invalid idempotency key returns 400 with stable error `code`.
- Idempotent replay is traceable in logs with correlation ID.

---

### EPIC P0-C: MCP Tool Surface with REST Parity
**Outcome:** AI-native callers can do the same core task operations.

#### TKT-P0-C1: Implement MCP tools
Tools: `create_task`, `get_task`, `list_tasks`, `update_task_status`.

**Acceptance criteria**
- Strict tool schemas + validation mirror REST constraints.
- Handlers call shared application service layer (no duplicate business logic).
- Tool-level tests cover happy path, not found, conflict.

#### TKT-P0-C2: Parity test suite (REST vs MCP)
**Acceptance criteria**
- Same command sequence produces equivalent state and error outcomes in both adapters.
- Parity tests are automated and documented in CI workflow.

---

### EPIC P0-D: API Contract Hardening
**Outcome:** Stable external contract and diagnosable failures.

#### TKT-P0-D1: OpenAPI generation + contract lock
**Acceptance criteria**
- OpenAPI doc generated from code and committed.
- Contract regression test prevents accidental breaking changes.

#### TKT-P0-D2: Correlation IDs + problem details consistency
**Acceptance criteria**
- Every error response includes stable `code` and `correlationId`.
- Correlation ID propagated from request header or generated server-side.

---

## P1 — Next increment after MVP
- Task event history (`task_events`) + timeline read endpoint/tool.
- Assignment lifecycle constraints (e.g., prevent assign when terminal).
- Cursor pagination for list APIs.
- Baseline metrics (latency/error rate by operation).

## P2 — Strategic
- Outbox/event publishing.
- Multi-tenant authorization boundaries.
- Archival + retention automation.

---

## Developer Handoff (next implementation slice)
1. **Persistence first:** introduce Mongo adapter behind current contract, keep controller signatures stable.
2. **Idempotency second:** add a small idempotency store abstraction before MCP work so both adapters share it.
3. **MCP third:** implement minimal 4-tool set and parity tests immediately (avoid semantic drift).

## Risks / Tradeoffs to watch
- **Current high risk:** in-memory store is non-durable and non-shareable across replicas.
- **Medium risk:** missing idempotency can create duplicate tasks during retries.
- **Medium risk:** REST and MCP divergence if handlers bypass common service layer.
