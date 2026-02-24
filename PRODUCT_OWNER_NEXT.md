# PRODUCT_OWNER_NEXT.md

Last updated: 2026-02-24 (PST, post-inspection refresh)
Owner: Product/Architecture

## 1) Code/docs/tests/CI inspection snapshot

### Verified baseline
- ✅ REST task APIs + lifecycle policy remain stable and test-covered.
- ✅ MCP task tools (`createTask`, `getTask`, `listTasks`, `updateTaskStatus`) are implemented against shared services.
- ✅ Cursor pagination contract is implemented across REST+MCP (`limit`, `cursor`, `nextCursor`).
- ✅ MCP caller-provided `correlationId` propagation with non-empty UUID fallback is implemented.
- ✅ CI gate runs `./gradlew check` on push/PR (`.github/workflows/ci.yml`, JDK 21).
- ✅ Contract gates in repo:
  - REST/MCP parity (`TaskRestMcpParityTest`)
  - MCP registration/runtime transport handshake (`TaskMcpToolRegistrationContractTest`, `TaskMcpRuntimeTransportContractTest`)
  - OpenAPI strict drift (`verifyOpenApiSnapshot`)
  - Error code contract stability (`ErrorCatalogContractTest`)
- ✅ Idempotency observability counters/docs in place (`agent_tracker_idempotency_events_total{event,operation}` + `OBSERVABILITY.md`).

### Fresh validation note
- ⚠️ Local `./gradlew check` could not execute in this environment because `JAVA_HOME` is not set.
- ⚠️ `openapi/openapi.yaml` appears stale relative to shipped pagination markers (cursor/limit/nextCursor), pending Java 21 snapshot regeneration.

### Confirmed active gaps
- 🟡 Pagination is currently service-layer slicing after full store read; large dataset efficiency risk remains.
- 🟡 OpenAPI snapshot refresh + CI green confirmation are pending maintainer run on Java 21.
- ✅ Deferred project DTOs are currently internalized under `agent.tracker.service.deferred.dto`.

---

## 2) Prioritized roadmap (REST + MCP readiness)

## P1 — Post-MVP hardening (active)

### EPIC P1-G: Contract governance completion
1. **TKT-P1-G15 — OpenAPI snapshot reconciliation for pagination/correlation updates** *(next coding slice)*

### EPIC P1-A: API contract hygiene and scale
2. **TKT-P1-A16 — Store-level pagination optimization with contract-preserving cursor semantics**

### EPIC P1-H: Surface hygiene
3. **TKT-P1-H14 — Keep deferred project DTO/contracts internal-only via regression guard posture**

## P2 — Strategic
- Outbox/event publishing
- Tenant/authz boundaries
- Archiving/lifecycle policy

---

## 3) Implementation-ready tickets (sharpened)

### TKT-P1-G15 — OpenAPI snapshot reconciliation for pagination/correlation updates
**Goal**  
Restore contract-gate alignment between generated OpenAPI output and checked-in snapshot after recent API/tool contract expansion.

**Scope**
- Run Java 21 toolchain locally/CI-compatible (`JAVA_HOME` set) and execute:
  - `./gradlew updateOpenApiSnapshot`
  - `./gradlew check`
- Commit regenerated `openapi/openapi.yaml` if changed.
- Verify snapshot includes pagination markers (`limit`, `cursor`, `nextCursor`) and current task route metadata.

**Acceptance criteria**
- `verifyOpenApiSnapshot` passes with strict generated-vs-snapshot equality.
- `OpenApiSnapshotContractTest` passes marker assertions, including pagination fields.
- CI `./gradlew check` job is green on branch/PR.

### TKT-P1-A16 — Store-level pagination optimization with contract-preserving cursor semantics
**Goal**  
Reduce list-query cost at scale while preserving the external REST/MCP pagination contract.

**Scope**
- Introduce store-level paginated list read path (Mongo-first) instead of in-service full-list slicing.
- Preserve external cursor opacity; maintain `limit/cursor/nextCursor` API shape.
- Keep deterministic ordering contract (`updatedAt DESC`, `taskId ASC`).
- Ensure parity tests still pass across REST and MCP.

**Acceptance criteria**
- Large-list retrieval avoids full filtered read before slicing (validated via code-path assertions/tests).
- External behavior remains unchanged for existing clients (including absent cursor/limit first-page behavior).
- REST/MCP parity tests cover multi-page progression and terminal-page semantics.
- Architecture docs updated with final pagination internals.

---

## 4) Recommended execution order
1. **Slice 1:** TKT-P1-G15 (OpenAPI snapshot + full check green)
2. **Slice 2:** TKT-P1-A16 (store-level pagination optimization)
3. **Slice 3:** Regression/cleanup pass on internal API surface hygiene and docs consistency

## 5) Active risks
- OpenAPI snapshot drift can block CI/PR confidence if not reconciled promptly.
- Current offset-style cursor implementation may have performance caveats under high mutation/large datasets.
- Local verification remains dependent on Java 21 + valid `JAVA_HOME`.

## 6) Developer handoff notes
- Canonical release gate remains `./gradlew check` in CI (JDK 21).
- Correlation policy is now finalized in `ADR-012` (supersedes interim ADR-011).
- Pagination contract is codified in `ADR-013`; implementation optimization is still pending.

## 7) 2026-02-24 developer run delta
- Hardened list ordering contract to `updatedAt DESC, taskId DESC` across in-memory and Mongo implementations.
- Updated Mongo repositories/indexes to match deterministic pagination ordering (`taskId` included in sort-support indexes).
- Finalized MCP correlation behavior implementation: propagate caller `correlationId` when valid UUID; generate UUID fallback for absent/blank/invalid inputs.
- Added `TaskQueryServiceTest` coverage for cursor/limit bounds and terminal-page `nextCursor=null` behavior.
- Extended MCP parity tests to assert invalid-correlation fallback produces a valid UUID.
- Added ADR-012 (final MCP correlation canonicalization; supersedes ADR-011) and ADR-013 (pagination ordering contract).
- Local validation blocker persists: Java 21 (`JAVA_HOME`) missing in this shell.
