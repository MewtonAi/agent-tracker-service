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
- ✅ MCP runtime transport test includes wire-level HTTP handshake (`initialize` + `tools/list`) and asserts discoverable tools + schema markers (`TaskMcpRuntimeTransportContractTest`).
- ✅ OpenAPI contract gate enforces strict generated-vs-checked-in equality (`verifyOpenApiSnapshot`) while retaining required marker checks.
- ✅ REST error-code lock suite added (`ErrorCatalogContractTest`) to stabilize external `code` contract.
- ✅ Idempotency replay observability event markers standardized (`idempotency.first_write`, `idempotency.replay_hit`, `idempotency.mismatch_reject`) with operation dimension.
- ✅ Idempotency metrics hardening landed: Micrometer/Prometheus counter emission (`agent_tracker_idempotency_events_total{event,operation}`), unit coverage, and dashboard/alert threshold documentation (`OBSERVABILITY.md`).

### Not shipped / not release-safe yet
- 🟡 Runtime dashboard wiring + production alert rollout still pending (repo-level metric instrumentation and threshold policy are complete).
- 🟡 API surface hygiene: deferred project DTO/contracts still present in public API package and can confuse client integrators.

---

## 2) Prioritized roadmap (REST + MCP readiness)

## P0 — MVP release gate
All current MVP release gates are complete and contract-tested.

## P1 — Post-MVP hardening (active)

### EPIC P1-O: Operational observability
1. ✅ **TKT-P1-O11 — Idempotency metrics/alerts hardening**
2. **TKT-P1-O12 — Correlation ID propagation test matrix (REST + MCP error paths)** *(next to implement)*

### EPIC P1-A: API contract hygiene
3. **TKT-P1-A13 — Task list cursor pagination contract (REST + MCP parity)**
4. **TKT-P1-A14 — Defer/internalize project DTO/contracts from outward API package**

## P2 — Strategic
- Outbox/event publishing
- Tenant/authz boundaries
- Archiving/lifecycle policy

---

## 3) Implementation-ready tickets (sharpened)

### TKT-P1-O11 — Idempotency metrics/alerts hardening
**Goal**
Turn existing idempotency log markers into actionable SRE signals.

**Scope**
- Emit counters for `first_write`, `replay_hit`, `mismatch_reject` by operation.
- Define replay ratio and mismatch rate formulas.
- Add dashboard spec and initial alert thresholds doc.

**Acceptance criteria**
- Counters exist and are incremented from command paths covered by tests.
- Metric dimensions include operation (e.g., `createTask`, `updateTaskStatus`).
- A dashboard definition (doc or JSON) shows replay ratio + mismatch rate.
- Alert thresholds and runbook notes are documented in repo.

### TKT-P1-A13 — Task list cursor pagination contract (REST + MCP parity)
**Goal**
Prevent unbounded list growth and align integration behavior across adapters.

**Scope**
- Add cursor-based pagination for list tasks endpoint/tool.
- Preserve backward compatibility for current callers (default first page behavior).

**Acceptance criteria**
- REST supports cursor parameters and returns deterministic next cursor.
- MCP `listTasks` surface exposes equivalent pagination fields.
- REST/MCP parity tests include pagination scenario equivalence.
- OpenAPI snapshot updated and gated.

### TKT-P1-A14 — API package hygiene for deferred project artifacts
**Goal**
Reduce ambiguity for external clients by removing non-supported project API shape from outward package.

**Scope**
- Move or deprecate deferred project DTO/contracts away from outward-facing API package.
- Clarify task-first scope in docs and contract tests.

**Acceptance criteria**
- Public API package no longer suggests unsupported project routes/features.
- Build/tests pass with no project-surface references in external docs/OpenAPI.
- Migration note included if any internal package rename affects imports.

---

## 4) Recommended execution order
1. **Slice 1:** TKT-P1-O12 (cross-channel error traceability)
2. **Slice 2:** TKT-P1-A13 (scalability + integrator ergonomics)
3. **Slice 3:** TKT-P1-A14 (surface clarity/maintenance)

---

## 5) Active risks to monitor
- Metrics/alerts gap can delay anomaly detection for idempotency spikes.
- Deferred project artifacts can cause client confusion and accidental unsupported integrations.
- Local CI confidence depends on Java 21 availability in contributor machines.

## 6) Developer handoff notes for next implementer
- `./gradlew check` is the canonical gate (parity + MCP runtime + OpenAPI drift + error catalog).
- Keep ADR-007 v2-only idempotency posture intact unless superseded via new ADR.
- If Micronaut MCP transport conventions change in upgrades, update transport contract tests before shipping.
