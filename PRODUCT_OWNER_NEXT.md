# PRODUCT_OWNER_NEXT.md

Last updated: 2026-02-24 (PST, post-inspection refresh)
Owner: Product/Architecture

## 1) Code/docs/tests/CI inspection snapshot

### Verified baseline
- ✅ REST task APIs + lifecycle policy remain stable and test-covered.
- ✅ MCP task tools (`createTask`, `getTask`, `listTasks`, `updateTaskStatus`) are implemented against shared services.
- ✅ CI gate runs `./gradlew check` on push/PR (`.github/workflows/ci.yml`, JDK 21).
- ✅ Contract gates in repo:
  - REST/MCP parity (`TaskRestMcpParityTest`)
  - MCP registration/runtime transport handshake (`TaskMcpToolRegistrationContractTest`, `TaskMcpRuntimeTransportContractTest`)
  - OpenAPI strict drift (`verifyOpenApiSnapshot`)
  - Error code contract stability (`ErrorCatalogContractTest`)
- ✅ Idempotency observability counters/docs in place (`agent_tracker_idempotency_events_total{event,operation}` + `OBSERVABILITY.md`).

### Fresh validation note
- ⚠️ Local `./gradlew check` could not execute in this environment because `JAVA_HOME` is not set.

### Confirmed active gaps
- 🟡 Task listing remains unpaginated (scalability + response-size risk).
- 🟡 MCP now guarantees error `correlationId`, but caller-supplied propagation semantics are not yet implemented/standardized.
- ✅ Deferred project DTOs are currently internalized under `agent.tracker.service.deferred.dto`.

---

## 2) Prioritized roadmap (REST + MCP readiness)

## P1 — Post-MVP hardening (active)

### EPIC P1-A: API contract hygiene and scale
1. **TKT-P1-A13 — Task list cursor pagination contract (REST + MCP parity)** *(next coding slice)*

### EPIC P1-O: Operational traceability
2. **TKT-P1-O12b — MCP caller-supplied correlation propagation semantics** *(follows pagination; ADR-011 supersession slice)*

### EPIC P1-H: Surface hygiene
3. **TKT-P1-A14 — Defer/internalize project DTO/contracts from outward API package** *(completed; keep regression guard posture)*

## P2 — Strategic
- Outbox/event publishing
- Tenant/authz boundaries
- Archiving/lifecycle policy

---

## 3) Implementation-ready tickets (sharpened)

### TKT-P1-A13 — Task list cursor pagination contract (REST + MCP parity)
**Goal**  
Prevent unbounded list growth while preserving deterministic, parity-safe integration behavior.

**Scope**
- Add cursor pagination (`limit`, `cursor`) to REST list endpoint.
- Expose equivalent fields in MCP `listTasks` request/response.
- Define deterministic ordering contract (recommended: `updatedAt DESC, taskId DESC` tie-break).
- Keep backward-compatible first-page behavior when pagination params are absent.

**Acceptance criteria**
- REST returns `{ tasks, nextCursor }` (or equivalent stable envelope) and honors bounded `limit`.
- MCP `listTasks` response includes matching pagination semantics and `nextCursor` behavior.
- REST/MCP parity tests include multi-page equivalence + end-of-stream (`nextCursor=null`) scenario.
- OpenAPI snapshot and MCP registration/runtime contract tests updated and passing.

### TKT-P1-O12b — MCP caller-supplied correlation propagation semantics
**Goal**  
Close the remaining traceability parity gap by defining how caller-provided correlation IDs flow through MCP failures.

**Scope**
- Choose source precedence for correlation tokens (tool arg vs transport metadata vs fallback generation).
- Implement chosen propagation strategy in MCP error path.
- Preserve existing mandatory fallback generation when caller value absent.
- Supersede ADR-011 with final policy ADR.

**Acceptance criteria**
- MCP exposes caller-provided correlation ID when present via chosen canonical source.
- MCP generates non-empty fallback correlation ID when caller token absent/invalid.
- Contract tests cover both propagation and fallback for representative error codes.
- Architecture/README/handoff docs reflect final parity semantics with REST.

---

## 4) Recommended execution order
1. **Slice 1:** TKT-P1-A13 (pagination contract + parity)
2. **Slice 2:** TKT-P1-O12b (caller-supplied correlation propagation policy + implementation)
3. **Slice 3:** Regression/cleanup pass on API surface hygiene and docs consistency

## 5) Active risks
- Unpaginated listing can become latency/memory risk with growth.
- Correlation propagation semantics still differ between REST and MCP for caller-supplied tokens.
- Local verification remains dependent on Java 21 + valid `JAVA_HOME`.

## 6) Developer handoff notes
- Canonical release gate remains `./gradlew check` in CI (JDK 21).
- New decision added: `ADR-011-mcp-correlation-id-source-policy.md` (interim MCP server-generated source policy).
- Next coding focus: pagination contract first; correlation source parity second.

## 7) 2026-02-24 developer run delta
- Implemented REST+MCP pagination envelope parity: limit + cursor inputs, 
extCursor output.
- Implemented MCP caller-provided correlationId propagation with UUID fallback in tool error mapping.
- Added/updated parity and registration/runtime tests for pagination + correlation schema fields.
- Local validation blocker: Java 21 (JAVA_HOME) missing, so ./gradlew check and OpenAPI snapshot refresh could not be executed in this shell.

