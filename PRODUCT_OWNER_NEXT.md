# PRODUCT_OWNER_NEXT.md

Last updated: 2026-02-24 (PST, architecture/product refresh)
Owner: Product/Architecture

## 1) Latest inspection snapshot (code/docs/tests/CI)

### Repo state validated
- ✅ REST + MCP task surfaces are implemented and parity-tested via shared application services.
- ✅ Pagination envelope is live in code (`limit`, `cursor`, `nextCursor`) with deterministic ordering policy (`updatedAt DESC, taskId DESC`).
- ✅ MCP correlation behavior is implemented with UUID canonicalization/fallback in `TaskMcpTools`.
- ✅ CI contract gate remains `./gradlew check` on JDK 21 (`.github/workflows/ci.yml`).
- ✅ Contract tests exist for parity, OpenAPI snapshot, error catalog, MCP runtime handshake, and idempotency telemetry.

### Risks / unresolved verification
- ⚠️ Local test execution blocked in this environment (`java`/`JAVA_HOME` unavailable), so current run is docs/architecture-only.
- ⚠️ `openapi/openapi.yaml` appears stale relative to shipped pagination fields (no cursor/limit/nextCursor markers present in snapshot file).
- ⚠️ ADR ambiguity detected: duplicate IDs for ADR-012 and ADR-013 (now governed by ADR-014 with canonical source policy).

---

## 2) Prioritized roadmap (REST + MCP readiness)

## P1 — Immediate release confidence

### EPIC P1-G: Contract governance completion
1. **TKT-P1-G15 — OpenAPI snapshot reconciliation + CI green confirmation** *(next coding slice)*
2. **TKT-P1-G17 — ADR canonicalization cleanup and reference hygiene**

### EPIC P1-A: Query scale hardening
3. **TKT-P1-A16 — Store-level pagination optimization with contract-preserving cursor semantics**

## P2 — Short-horizon platform hardening
- Authentication/tenant boundaries
- Domain event/outbox shape for downstream integrations
- Archive/retention lifecycle policy

---

## 3) Implementation-ready tickets (acceptance-criteria grade)

### TKT-P1-G15 — OpenAPI snapshot reconciliation + CI green confirmation
**Goal**  
Restore strict OpenAPI contract gate alignment with currently shipped REST contract.

**Scope**
- Run on Java 21 environment:
  - `./gradlew updateOpenApiSnapshot`
  - `./gradlew check`
- Commit regenerated `openapi/openapi.yaml`.
- Verify snapshot includes task-list pagination query/response fields (`limit`, `cursor`, `nextCursor`).

**Acceptance criteria**
- `verifyOpenApiSnapshot` passes with strict equality.
- `OpenApiSnapshotContractTest` passes marker assertions.
- CI workflow completes green on PR branch.

---

### TKT-P1-G17 — ADR canonicalization cleanup and reference hygiene
**Goal**  
Eliminate policy ambiguity from duplicate ADR numbering and make canonical contract sources explicit.

**Scope**
- Apply ADR-014 policy to existing docs and references.
- Ensure superseded ADRs are labeled as historical only.
- Update any docs/tests still pointing at superseded ADR variants.

**Acceptance criteria**
- Exactly one canonical ADR filename is referenced per active topic in architecture/product docs.
- Superseded ADRs include explicit forward pointers to canonical files.
- No new planning docs reference superseded ADR variants as active policy.

---

### TKT-P1-A16 — Store-level pagination optimization with contract-preserving cursor semantics
**Goal**  
Improve list performance at scale while preserving REST/MCP external contract.

**Scope**
- Move from full-list then slice toward DB/store-backed page reads.
- Keep current response envelope and validation semantics unchanged.
- Preserve deterministic ordering contract (`updatedAt DESC, taskId DESC`).
- Maintain parity coverage across REST and MCP.

**Acceptance criteria**
- Data path avoids loading all filtered rows before page slicing on Mongo-backed store.
- Existing clients observe unchanged API/tool behavior for default and explicit paging.
- Parity tests include multi-page progression and terminal `nextCursor=null` behavior.
- Architecture docs describe old vs new paging internals and migration safety.

---

## 4) Recommended execution order
1. **Slice 1:** TKT-P1-G15 (OpenAPI reconciliation + CI confidence)
2. **Slice 2:** TKT-P1-G17 (ADR/source-of-truth cleanup pass)
3. **Slice 3:** TKT-P1-A16 (store-level pagination optimization)

## 5) Active risk register
- OpenAPI snapshot drift can continue to block merge confidence until regenerated under Java 21.
- Offset-style cursor remains vulnerable to large-offset performance degradation until store-level paging is implemented.
- Contract ambiguity may reappear if duplicate ADR numbering is not kept under ADR-014 governance.

## 6) Developer handoff anchor points
- Canonical CI gate: `./gradlew check` (JDK 21).
- Canonical MCP correlation policy: `ADR-012-mcp-correlation-id-canonicalization-policy.md`.
- Canonical pagination policy: `ADR-013-task-list-pagination-ordering-contract.md`.
- Canonical governance for duplicates/supersession: `ADR-014-contract-source-of-truth-and-supersession-policy.md`.
