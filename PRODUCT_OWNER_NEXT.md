# PRODUCT_OWNER_NEXT.md

Last updated: 2026-02-24 (PST, late-night refresh)
Owner: Product/Architecture

## 1) Code/docs/tests/CI inspection snapshot

### Verified shipped baseline
- ✅ REST task APIs + lifecycle policy are stable and test-covered.
- ✅ MCP task tools (`createTask`, `getTask`, `listTasks`, `updateTaskStatus`) are implemented against shared services.
- ✅ CI gate runs `./gradlew check` on push/PR (`.github/workflows/ci.yml`, JDK 21).
- ✅ Contract gates in repo:
  - REST/MCP parity (`TaskRestMcpParityTest`)
  - MCP registration/runtime transport handshake (`TaskMcpToolRegistrationContractTest`, `TaskMcpRuntimeTransportContractTest`)
  - OpenAPI strict drift (`verifyOpenApiSnapshot`)
  - Error code contract stability (`ErrorCatalogContractTest`)
- ✅ Idempotency observability counters/docs are in place (`agent_tracker_idempotency_events_total{event,operation}` + `OBSERVABILITY.md`).

### Confirmed gaps (post-MVP)
- 🟡 MCP `correlationId` now exists on tool exceptions and has regression coverage, but caller-supplied propagation semantics are still not defined.
- 🟡 Task listing remains unpaginated (growth/scalability risk).
- ✅ Deferred project DTOs moved to internal deferred namespace (`agent.tracker.service.deferred.dto`).

---

## 2) Prioritized roadmap (REST + MCP readiness)

## P0 — MVP release gate
- Complete.

## P1 — Post-MVP hardening (active)

### EPIC P1-O: Operational traceability + observability
1. **TKT-P1-O12 — Cross-transport correlation ID propagation contract (REST + MCP)** *(in progress: MCP error correlationId + matrix tests landed; caller-token propagation pending)*

### EPIC P1-A: API contract hygiene and scale
2. **TKT-P1-A13 — Task list cursor pagination contract (REST + MCP parity)** *(next)*
3. ✅ **TKT-P1-A14 — Defer/internalize project DTO/contracts from outward API package**

## P2 — Strategic
- Outbox/event publishing
- Tenant/authz boundaries
- Archiving/lifecycle policy

---

## 3) Implementation-ready tickets (sharpened)

### TKT-P1-O12 — Cross-transport correlation ID propagation contract
**Goal**
Make failure triage transport-agnostic by guaranteeing a correlation token in both REST and MCP error paths.

**Scope**
- Define and implement MCP tool error payload `correlationId` (with stable `code` + message retained).
- Reuse caller-provided correlation token when present; generate fallback when missing.
- Add REST+MCP error path tests for parity scenarios.
- Update docs/contract references (ADR/architecture/handoff).

**Acceptance criteria**
- REST behavior remains: echoes `X-Correlation-Id` or generates one.
- MCP failures expose non-empty `correlationId` for representative domain/application errors.
- Parity test matrix includes at least:
  - not found (`TASK_NOT_FOUND`)
  - optimistic conflict (`CONCURRENT_MODIFICATION`)
  - idempotency mismatch (`IDEMPOTENCY_KEY_REUSE_MISMATCH`)
- Contract tests fail if `correlationId` disappears from either transport’s error surface.

### TKT-P1-A13 — Task list cursor pagination contract (REST + MCP parity)
**Goal**
Prevent unbounded list growth while preserving deterministic, parity-safe integration behavior.

**Scope**
- Add cursor pagination (`limit`, `cursor`) to REST list endpoint.
- Expose equivalent fields in MCP `listTasks` request/response.
- Keep default first-page semantics for existing callers.

**Acceptance criteria**
- REST returns deterministic `nextCursor` when additional rows exist.
- MCP list result carries matching pagination semantics.
- REST/MCP parity tests include multi-page equivalence scenario.
- OpenAPI snapshot updated and CI-gated.

### TKT-P1-A14 — API package hygiene for deferred project artifacts
**Goal**
Reduce client confusion by removing unsupported project surface cues from public API package/docs.

**Scope**
- Move/deprecate deferred project DTO/contracts to internal package namespace.
- Ensure OpenAPI/README/product docs remain task-first and route-accurate.

**Acceptance criteria**
- Public API package no longer implies project route support.
- No outward docs advertise deferred project APIs.
- Build/tests pass with updated package references.
- Migration note included for internal import moves.

---

## 4) Recommended execution order
1. **Slice 1:** TKT-P1-O12 (traceability parity)
2. **Slice 2:** TKT-P1-A13 (pagination contract)
3. **Slice 3:** TKT-P1-A14 (surface hygiene)

## 5) Active risks
- Inconsistent correlation behavior between adapters can slow incident triage.
- Unpaginated listing can become a latency/memory risk with growth.
- Deferred project artifacts still create accidental integration ambiguity.
- Local contributor verification still depends on Java 21 availability.

## 6) Developer handoff notes
- Canonical release gate remains `./gradlew check`.
- New governance decision: ADR-010 locks cross-transport correlation ID expectations.
- Preserve ADR-007 (idempotency v2-only posture) unless superseded.
- If Micronaut MCP transport conventions shift, update runtime transport contract tests in same slice.
