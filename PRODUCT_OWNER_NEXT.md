# PRODUCT_OWNER_NEXT.md

Last updated: 2026-02-24 (PST)
Owner: Product/Architecture

## 1) Code-verified implementation snapshot

### Shipped now
- ✅ Canonical v1 execution path is **Task-first** and REST-operational.
- ✅ Lifecycle + transition policy are enforced in domain/application flow.
- ✅ REST endpoints shipped:
  - `POST /v1/tasks` (Idempotency-Key required)
  - `GET /v1/tasks/{id}`
  - `GET /v1/tasks?status=`
  - `PATCH /v1/tasks/{id}/status` (Idempotency-Key required)
- ✅ Invalid `status` query now returns explicit contract-level 400 (`BAD_REQUEST`).
- ✅ Concurrency conflicts map to stable code `CONCURRENT_MODIFICATION`.
- ✅ Mongo persistence seam exists with optimistic locking (`@Version`).

### Not shipped / not release-safe yet
- ❌ MCP tool surface not implemented (`create_task`, `get_task`, `list_tasks`, `update_task_status`).
- ❌ REST/MCP parity suite not implemented.
- ❌ Durable idempotency v2 incomplete:
  - key uniqueness still key-only (not `(operation,key)`)
  - no payload fingerprint mismatch detection
  - TTL tied to `createdAt` instead of explicit `expiresAt`
- ❌ OpenAPI generation/snapshot/diff gate missing.

---

## 2) Prioritized roadmap (REST + MCP readiness)

## P0 — MVP release gate

### EPIC P0-A: Idempotency v2 completion (highest risk)
1. **TKT-P0-A1 — Schema/index v2 rollout (ADR-005)**
   - Add explicit fields: `operation`, `key`, `payloadHash`, `resultRef`, `expiresAt`, timestamps.
   - Add unique index on `(operation,key)` and TTL index on `expiresAt`.
   - Phase A dual-read compatibility, Phase B cleanup.

2. **TKT-P0-A2 — Mismatch policy enforcement**
   - Same `(operation,key)` + different payload hash => 409 `IDEMPOTENCY_KEY_REUSE_MISMATCH`.

3. **TKT-P0-A3 — Idempotency observability**
   - Emit counters/logs: `idempotency.first_write`, `idempotency.replay_hit`, `idempotency.mismatch_reject`, `idempotency.legacy_fallback_hit`.

### EPIC P0-B: MCP tool delivery + parity
4. **TKT-P0-B1 — Implement 4 MCP tools via shared services**
5. **TKT-P0-B2 — REST/MCP parity scenario suite in CI**

### EPIC P0-C: Contract governance
6. **TKT-P0-C1 — OpenAPI generation + snapshot + drift gate**
7. **TKT-P0-C2 — Error catalog lock tests (`code` stability)**

## P1 — Post-MVP
- Cursor pagination for list API
- Task event timeline/read model
- Metrics dashboard and baseline SLOs
- Remove stale/deferred project-surface artifacts from public contracts

## P2 — Strategic
- Outbox/event publishing
- Tenant/authz boundaries
- Archival lifecycle policy

---

## 3) Implementation-ready tickets (with acceptance criteria)

### TKT-P0-A1 — Idempotency schema/index v2 rollout
**Scope**
- Introduce v2 document/index model and rollout-safe read/write strategy.

**Acceptance criteria**
- New writes persist `operation,key,payloadHash,resultRef,expiresAt`.
- Startup creates unique `(operation,key)` + TTL(`expiresAt`) indexes.
- Replay path checks v2 first; optional legacy fallback is feature-flagged/config-bounded.
- Integration test proves replay continuity for pre-v2 and v2 records during compatibility window.

### TKT-P0-A2 — Idempotency mismatch rejection
**Scope**
- Detect payload mismatch and reject deterministically.

**Acceptance criteria**
- Same operation+key+same payload => replay success.
- Same operation+key+different payload => HTTP 409, `IDEMPOTENCY_KEY_REUSE_MISMATCH`.
- Error response keeps RFC7807 shape + correlation ID.

### TKT-P0-B1 — MCP 4-tool adapter
**Scope**
- Add MCP tools: create/get/list/update-status.
- Keep transport mapping thin; no business rule duplication.

**Acceptance criteria**
- Tool schema validation mirrors REST constraints.
- Tool errors map to same semantic code set as REST.
- Unit/integration tests cover happy path, not found, invalid transition, replay, mismatch.

### TKT-P0-B2 — REST/MCP parity suite
**Scope**
- Scenario-driven parity harness running identical intent via both adapters.

**Acceptance criteria**
- Equivalent final state across adapters.
- Equivalent error category/code across adapters.
- CI job fails on parity drift.

### TKT-P0-C1 — OpenAPI contract lock
**Scope**
- Generate and check in `openapi.yaml`; enforce diff gate in CI.

**Acceptance criteria**
- Snapshot is versioned in repo.
- CI fails on contract drift unless snapshot update is explicit in PR.

---

## 4) Recommended execution order (next 2 slices)

**Slice 1 (risk retirement):** TKT-P0-A1, TKT-P0-A2, TKT-P0-A3

**Slice 2 (feature completion):** TKT-P0-B1, TKT-P0-B2, TKT-P0-C1, TKT-P0-C2

---

## 5) Active risks to monitor
- Hardcoded Mongo DB name in index initializer may diverge from runtime config.
- Migration complexity if legacy idempotency read-path is left unbounded.
- MCP delay increases chance of semantic drift between adapters.
