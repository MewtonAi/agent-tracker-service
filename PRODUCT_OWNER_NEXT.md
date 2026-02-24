# PRODUCT_OWNER_NEXT.md

Last updated: 2026-02-24 (PST)
Owner: Product/Architecture

## 1) Code-verified implementation snapshot

### Shipped now
- ✅ Task-first v1 REST execution path is operational.
- ✅ Lifecycle and transition policy are enforced in domain/application flow.
- ✅ REST endpoints shipped:
  - `POST /v1/tasks` (Idempotency-Key required)
  - `GET /v1/tasks/{id}`
  - `GET /v1/tasks?status=`
  - `PATCH /v1/tasks/{id}/status` (Idempotency-Key required)
- ✅ Invalid `status` query returns explicit 400 (`BAD_REQUEST`).
- ✅ Concurrency conflicts map to stable code `CONCURRENT_MODIFICATION`.
- ✅ Durable idempotency v2 is in place:
  - unique scope `(operation,key)`
  - payload fingerprint mismatch rejection (`IDEMPOTENCY_KEY_REUSE_MISMATCH`)
  - TTL index on explicit `expiresAt`
  - retention configurable via `idempotency.ttl-hours` (default 48h)

### Not shipped / not release-safe yet
- ✅ MCP application tool surface implemented via `TaskMcpTools` (`createTask`, `getTask`, `listTasks`, `updateTaskStatus`) sharing existing services.
- ✅ REST/MCP parity test baseline added (`TaskRestMcpParityTest`) for create, transition, and idempotency-mismatch semantics.
- 🟡 MCP transport-level schema/registration contract tests added at code level (`TaskMcpToolRegistrationContractTest`), but runtime transport-wire verification is still pending.
- ❌ OpenAPI generation/snapshot/diff gate missing.
- ✅ Idempotency replay observability event markers standardized (`idempotency.first_write`, `idempotency.replay_hit`, `idempotency.mismatch_reject`) with operation dimension.

---

## 2) Prioritized roadmap (REST + MCP readiness)

## P0 — MVP release gate

### EPIC P0-A: MCP delivery + semantic parity (highest impact)
1. ✅ **TKT-P0-A1 — Implement 4 MCP tools via shared services**
2. 🟡 **TKT-P0-A2 — REST/MCP parity scenario suite in CI** (baseline scenarios implemented; CI gate still pending)
3. **TKT-P0-A3 — MCP runtime registration/schema verification**

### EPIC P0-B: Contract governance
3. **TKT-P0-B1 — OpenAPI generation + snapshot + drift gate**
4. **TKT-P0-B2 — Error catalog lock tests (`code` stability)**

### EPIC P0-C: Idempotency operations readiness
5. **TKT-P0-C1 — Idempotency replay observability**
6. ✅ **TKT-P0-C2 — Migration decision log completed (ADR-007): v2-only posture, no legacy fallback**

## P1 — Post-MVP hardening
- Cursor pagination for list API
- Task event timeline/read model
- Metrics dashboard and baseline SLOs
- Remove stale/deferred project-surface DTO/contracts from public API package

## P2 — Strategic
- Outbox/event publishing
- Tenant/authz boundaries
- Archival lifecycle policy

---

## 3) Implementation-ready tickets (with acceptance criteria)

### TKT-P0-A1 — MCP 4-tool adapter
**Scope**
- Add MCP tools: `create_task`, `get_task`, `list_tasks`, `update_task_status`.
- Keep transport mapping thin; no business-rule duplication.

**Acceptance criteria**
- Tool schemas enforce same required fields/validation semantics as REST.
- Tool handlers delegate to existing application services.
- Tool error responses map to same semantic code set as REST.
- Tests cover happy path, not found, invalid transition, replay, mismatch.

### TKT-P0-A2 — REST/MCP parity suite
**Scope**
- Scenario-driven parity harness runs identical intent through both adapters.

**Acceptance criteria**
- Equivalent final state across adapters.
- Equivalent error category/code across adapters.
- CI job fails on parity drift.

### TKT-P0-A3 — MCP runtime registration/schema verification
**Scope**
- Verify tool registration and request schema exposure in an actual MCP runtime session.
- Capture a reproducible smoke check (local command + expected tool inventory).

**Acceptance criteria**
- Runtime reports all 4 task tools discoverable.
- Required input fields align with current request records (`idempotencyKey`, `taskId`, etc.).
- Smoke check is documented and executable by any developer.

### TKT-P0-B1 — OpenAPI contract lock
**Scope**
- Generate and check in `openapi.yaml`; enforce snapshot drift gate in CI.

**Acceptance criteria**
- Snapshot file versioned in repo.
- CI fails on drift unless snapshot update is explicit in PR.
- Error examples include `CONCURRENT_MODIFICATION` and `IDEMPOTENCY_KEY_REUSE_MISMATCH`.

### TKT-P0-B2 — Error catalog stability tests
**Scope**
- Lock externally visible `code` values used by REST errors.

**Acceptance criteria**
- Contract tests fail on unreviewed code renames/removals.
- ADR references included in test documentation/comments.

### TKT-P0-C1 — Idempotency replay observability
**Scope**
- Emit structured counters/logs for replay lifecycle.

**Acceptance criteria**
- Counters/log markers: `idempotency.first_write`, `idempotency.replay_hit`, `idempotency.mismatch_reject`.
- Metric/log dimensions include operation name.
- Replay/mismatch integration tests assert signal emission (or equivalent hook).

### TKT-P0-C2 — Legacy migration decision record ✅ Done
**Status**
- Completed via ADR-007 (`idempotency-v2-only-posture`).

**Implemented decision**
- Repository posture is explicitly v2-only (`operation`, `key`, `payloadHash`, `expiresAt`) with no legacy key-only replay fallback.

**Follow-on enforcement (remaining hardening)**
- Add a targeted test/doc check that fails if fallback semantics are reintroduced without ADR.

---

## 4) Recommended execution order (next 2 slices)

**Slice 1 (MVP unlock):** TKT-P0-A2, TKT-P0-A3

**Slice 2 (contract + ops hardening):** TKT-P0-B1, TKT-P0-B2, TKT-P0-C1

**Already completed decision work:** TKT-P0-C2 (ADR-007)

---

## 5) Active risks to monitor
- MCP implementation delay increases chance of semantic drift from REST.
- OpenAPI drift without CI gate can create silent breaking changes.
- Deferred project DTOs in API package can confuse clients about supported v1 surface.

## 6) Developer update (2026-02-24 run)
- Added REST error-code lock suite: src/test/java/agent/tracker/service/api/ErrorCatalogContractTest.java (ADR-003/004/006 guardrail).
- Added idempotency observability hooks and signals:
  - interface: IdempotencyTelemetry
  - default implementation: LoggingIdempotencyTelemetry
  - emitted markers: idempotency.first_write, idempotency.replay_hit, idempotency.mismatch_reject (with operation name).
  - tests: TaskCommandServiceObservabilityTest
- Added MCP tool registration/schema guardrail test: TaskMcpToolRegistrationContractTest (code-level contract; runtime wire check still pending).
- Next planning focus: TKT-P0-B1 OpenAPI snapshot + CI drift gate, and TKT-P0-A3 runtime MCP smoke registration check in CI.
