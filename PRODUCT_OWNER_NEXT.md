# PRODUCT_OWNER_NEXT.md

Last updated: 2026-02-24 (PST, late)
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
- ✅ MCP application tool surface implemented via `TaskMcpTools` (`createTask`, `getTask`, `listTasks`, `updateTaskStatus`) sharing existing services.
- ✅ REST/MCP parity suite present and CI-gated via `./gradlew check` (`TaskRestMcpParityTest`).
- ✅ MCP code-level registration/schema contract tests added (`TaskMcpToolRegistrationContractTest`).
- ✅ MCP runtime boot + bean/method surface test present (`TaskMcpRuntimeTransportContractTest`).
- ✅ OpenAPI baseline snapshot + CI verification task is wired (`verifyOpenApiSnapshot`).
- ✅ REST error-code lock suite added (`ErrorCatalogContractTest`) to stabilize external `code` contract.
- ✅ Idempotency replay observability event markers standardized (`idempotency.first_write`, `idempotency.replay_hit`, `idempotency.mismatch_reject`) with operation dimension.

### Not shipped / not release-safe yet
- 🟡 MCP runtime verification still stops at context + reflection checks; explicit transport-wire `tools/list` handshake verification is pending.
- 🟡 OpenAPI gate still checks marker presence only; strict generated-vs-checked-in equality enforcement is pending.

---

## 2) Prioritized roadmap (REST + MCP readiness)

## P0 — MVP release gate

### EPIC P0-A: MCP delivery + semantic parity (highest impact)
1. ✅ **TKT-P0-A1 — Implement 4 MCP tools via shared services**
2. ✅ **TKT-P0-A2 — REST/MCP parity scenario suite in CI**
3. 🟡 **TKT-P0-A3 — MCP transport-level runtime handshake verification**

### EPIC P0-B: Contract governance
4. 🟡 **TKT-P0-B1 — OpenAPI strict snapshot drift gate (generated-vs-checked-in)**
5. ✅ **TKT-P0-B2 — Error catalog lock tests (`code` stability)**

### EPIC P0-C: Idempotency operations readiness
6. ✅ **TKT-P0-C1 — Idempotency replay observability baseline (structured markers + tests)**
7. ✅ **TKT-P0-C2 — Migration decision log completed (ADR-007): v2-only posture, no legacy fallback**

## P1 — Post-MVP hardening
- Promote idempotency observability from log markers to durable metrics dashboards/alerts
- Cursor pagination for list API
- Task event timeline/read model
- Remove stale/deferred project-surface DTO/contracts from public API package

## P2 — Strategic
- Outbox/event publishing
- Tenant/authz boundaries
- Archival lifecycle policy

---

## 3) Implementation-ready tickets (with acceptance criteria)

### TKT-P0-A3 — MCP transport-level runtime handshake verification
**Scope**
- Validate tool registration and request schema through MCP transport discovery (not reflection-only tests).
- Capture reproducible smoke command(s) in README/runbook.

**Acceptance criteria**
- Runtime `tools/list` reports all 4 task tools discoverable.
- Required fields align with request records (`idempotencyKey`, `taskId`, etc.).
- Smoke check is executable by any developer (single documented command sequence).
- CI (or pre-merge job) enforces the smoke verification result.

### TKT-P0-B1 — OpenAPI strict contract lock
**Scope**
- Enforce deterministic generated-vs-checked-in OpenAPI equality in CI.

**Acceptance criteria**
- Snapshot file (`openapi/openapi.yaml`) is committed in repo.
- CI generates OpenAPI and fails when diff exists unless snapshot update is included in PR.
- Error examples include `CONCURRENT_MODIFICATION` and `IDEMPOTENCY_KEY_REUSE_MISMATCH`.
- Developer docs include exact regenerate command.

### TKT-P1-O11 — Idempotency metrics/alerts hardening (post-MVP)
**Scope**
- Extend existing observability markers into durable metrics + alert thresholds.

**Acceptance criteria**
- Counters exported for first-write/replay/mismatch by operation.
- Dashboard shows replay ratio and mismatch rate.
- Alert thresholds documented for anomaly conditions.

---

## 4) Recommended execution order (next 2 slices)

**Slice 1 (MVP unlock):** TKT-P0-A3

**Slice 2 (contract hardening):** TKT-P0-B1

**Post-MVP:** TKT-P1-O11

---

## 5) Active risks to monitor
- MCP runtime registration can still drift from code-level contract tests without a transport-wire gate.
- OpenAPI marker-only checks can miss structural contract drift.
- Deferred project DTOs in API package can still confuse clients about supported v1 surface.

## 6) Developer handoff notes for next implementer
- ✅ Parity + contract checks run via GitHub Actions (`./gradlew check`).
- **Next unblock:** extend MCP runtime check to explicit transport-wire `tools/list` handshake (`TKT-P0-A3`).
- **Next contract hardening:** move from OpenAPI marker checks to generated-vs-snapshot strict diff (`TKT-P0-B1`).
- Keep ADR-007 posture intact (v2-only idempotency semantics) unless superseded by a new ADR.
