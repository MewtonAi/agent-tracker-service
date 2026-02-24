# PRODUCT_OWNER_NEXT.md

Last updated: 2026-02-23 (PST)
Owner: Product/Architecture

## 1) Code-verified implementation snapshot

### Shipped now
- ✅ Canonical v1 scope is **Task-only**.
- ✅ Canonical lifecycle in code: `NEW, IN_PROGRESS, BLOCKED, DONE, CANCELED`.
- ✅ Transition policy enforcement is implemented.
- ✅ REST endpoints shipped:
  - `POST /v1/tasks` (Idempotency-Key required)
  - `GET /v1/tasks/{id}`
  - `GET /v1/tasks?status=`
  - `PATCH /v1/tasks/{id}/status` (Idempotency-Key required)
- ✅ RFC7807-style API error envelope + correlation ID support.
- ✅ Mongo persistence seam and adapter exist (`task.store=mongo`).
- ✅ `TaskDocument` has `@Version` optimistic locking field.
- ✅ Mongo integration test covers persistence + basic idempotent replay behavior.

### Not shipped / not release-safe yet
- ❌ MCP tool surface not implemented (`create_task`, `get_task`, `list_tasks`, `update_task_status`).
- ❌ REST/MCP parity suite not implemented.
- 🟡 Durable idempotency model is partial (key only; no operation + payload fingerprint + TTL policy).
- 🟡 Conflict semantics not fully hardened (current code: `TASK_CONFLICT`; ADR target suggests `CONCURRENT_MODIFICATION`).
- 🟡 Index strategy/docs drift: architecture target and actual initializer differ.
- 🟡 Invalid `status` query handling is framework-leaky (`TaskStatus.valueOf`) and needs explicit contract mapping.
- ❌ OpenAPI generation/snapshot/diff gate missing.

---

## 2) Prioritized roadmap (REST + MCP readiness)

## P0 — MVP release gate

### EPIC P0-A: Mutation correctness + contract hardening
Goal: deterministic, documented mutation behavior under retries and concurrency.

1. **TKT-P0-A1 — Concurrency error contract lock**
   - Decide and lock one stable 409 code for optimistic-lock conflicts.
   - Recommended: `CONCURRENT_MODIFICATION`.
   - Add tests + docs alignment.

2. **TKT-P0-A2 — Explicit status filter validation**
   - Replace raw `TaskStatus.valueOf` leak with adapter-level validation/mapping.
   - Invalid enum input must return stable 400 shape/code.

3. **TKT-P0-A3 — Same-key different-payload policy**
   - Define behavior for idempotency-key reuse with divergent payload.
   - Recommended: reject with 409 and explicit code (e.g., `IDEMPOTENCY_KEY_REUSE_MISMATCH`).

### EPIC P0-B: Durable idempotency completion
Goal: production-safe replay behavior across restarts/replicas.

4. **TKT-P0-B1 — Idempotency record schema v2**
   - Add fields: `operation`, `payloadHash`, `resultRef`, `expiresAt`, timestamps.
   - Keep unique constraint on `(operation, key)`.

5. **TKT-P0-B2 — TTL + retention policy**
   - Add TTL index on `expiresAt`.
   - Make retention configurable (default: 48h).

6. **TKT-P0-B3 — Replay observability**
   - Emit structured logs/metrics for `first_write`, `replay_hit`, `mismatch_reject`.

### EPIC P0-C: MCP parity delivery
Goal: agent-native interface with identical business semantics.

7. **TKT-P0-C1 — Implement 4 MCP tools via shared services**
8. **TKT-P0-C2 — Cross-adapter parity test harness** (REST vs MCP scenario equivalence)

### EPIC P0-D: Contract governance
Goal: minimize accidental breaking changes.

9. **TKT-P0-D1 — OpenAPI generation + checked-in snapshot + CI diff gate**
10. **TKT-P0-D2 — Error catalog and regression tests**

## P1 — After MVP
- Cursor pagination for task list
- `task_events` timeline read model
- metrics dashboard + baseline SLO

## P2 — Strategic
- outbox/event publishing
- tenant/authz boundaries
- archival lifecycle policy

---

## 3) Implementation-ready tickets (with acceptance criteria)

### TKT-P0-A1 — Concurrency error contract lock
**Scope**
- Standardize optimistic-lock exception mapping to one code.
- Align ADR/docs/tests.

**Acceptance criteria**
- Concurrent update integration test produces HTTP 409 with chosen stable code.
- Error code appears in API docs and regression tests.
- No remaining references to alternate concurrency codes in v1 docs.

### TKT-P0-A2 — Status filter validation hardening
**Scope**
- Parse/validate query status safely in controller or dedicated mapper.

**Acceptance criteria**
- `/v1/tasks?status=NOT_A_STATUS` returns 400 with stable code and RFC7807 body.
- Response includes `correlationId` and `X-Correlation-Id`.
- Behavior covered by controller test.

### TKT-P0-B1/B2 — Idempotency v2 + TTL
**Scope**
- Evolve idempotency document and unique index to `(operation, key)`.
- Persist payload hash and expiry.
- Add TTL index on `expiresAt`.

**Acceptance criteria**
- Replayed request with same operation+key+payload returns original logical result.
- Same operation+key with different payload is rejected per policy.
- Expiry/TTL index is created at startup and documented.

### TKT-P0-C1 — MCP 4-tool adapter
**Scope**
- Add MCP tools: create/get/list/update-status.
- Handlers only map transport ↔ application contracts.

**Acceptance criteria**
- Each tool has schema validation mirroring REST constraints.
- Tool tests cover: happy path, not found, invalid transition, idempotent replay.
- No duplicated business rules in MCP adapter.

### TKT-P0-C2 — REST/MCP parity suite
**Scope**
- Scenario-driven suite executes equivalent command sequences through both adapters.

**Acceptance criteria**
- Equivalent final task state across adapters for same scenarios.
- Equivalent error semantics (status/code/category) across adapters.
- Suite runs in CI and referenced in README.

### TKT-P0-D1 — OpenAPI contract lock
**Scope**
- Generate OpenAPI from runtime/controller metadata.
- Commit snapshot and enforce CI diff check.

**Acceptance criteria**
- `openapi.yaml` generated and versioned.
- CI fails on contract drift unless snapshot updated in PR.

---

## 4) Recommended build order (next 2 dev slices)

**Slice 1 (highest risk retirement):** P0-A1, P0-A2, P0-B1/B2
- Locks external behavior before adding adapter surface.

**Slice 2 (feature completion):** P0-C1, P0-C2, P0-D1
- Delivers MCP with confidence via parity + contract governance.

---

## 5) Risks to watch now
- Hardcoded DB name in Mongo index initializer can drift from runtime config.
- Idempotency uniqueness currently key-only, not operation-scoped in schema.
- Current replay model stores taskId only; payload mismatch cannot be detected reliably.
- Adapter parity risk increases the longer MCP implementation is delayed.
