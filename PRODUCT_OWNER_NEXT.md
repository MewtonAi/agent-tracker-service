# PRODUCT_OWNER_NEXT.md

Last updated: 2026-02-24 (PST, product/architecture continuity pass)
Owner: Product/Architecture

## 1) Latest inspection snapshot (code/docs/tests/CI)

### Repo state validated
- ✅ REST + MCP task surfaces remain parity-tested via shared application services.
- ✅ Store-level pagination seam is now implemented (`TaskStore#listTasksPage`) and wired through Mongo `Pageable` + `Slice`.
- ✅ Deterministic ordering contract is implemented in Mongo path (`updatedAt DESC, taskId DESC`) per canonical ADR-013.
- ✅ MCP correlation canonicalization/fallback behavior is implemented in `TaskMcpTools` and governed by canonical ADR-012.
- ✅ CI gate remains minimal and clear: `./gradlew check` on JDK 21 (`.github/workflows/ci.yml`).

### Active risks / unresolved verification
- ⚠️ Local execution is blocked in this shell (`java`/`JAVA_HOME` unavailable), so test/CI verification could not be re-run in this pass.
- ⚠️ `openapi/openapi.yaml` appears stale relative to shipped pagination contract (`limit`, `cursor`, `nextCursor`) and likely fails current marker assertions.
- ⚠️ Offset cursor remains the external token shape today; scale posture is improved by store paging but still needs documented evolution path for very large offsets.

---

## 2) Prioritized roadmap (REST + MCP readiness)

## P1 — Immediate release confidence

### EPIC P1-G: Contract governance + release gate confidence
1. **TKT-P1-G15 — OpenAPI snapshot reconciliation + CI green confirmation** *(next coding slice)*
2. **TKT-P1-G17 — ADR canonicalization cleanup and reference hygiene**

### EPIC P1-A: Pagination durability at scale
3. **TKT-P1-A18 — Cursor evolution readiness (dual-decode strategy + compatibility tests)**

## P2 — Short-horizon platform hardening
- Authn/authz and tenant boundaries
- Domain event/outbox shape for downstream integrations
- Archive/retention lifecycle policy

---

## 3) Implementation-ready tickets (acceptance-criteria grade)

### TKT-P1-G15 — OpenAPI snapshot reconciliation + CI green confirmation
**Goal**  
Restore strict OpenAPI gate alignment with currently shipped REST contract.

**Scope**
- Run on Java 21 environment:
  - `./gradlew updateOpenApiSnapshot`
  - `./gradlew check`
- Commit regenerated `openapi/openapi.yaml`.
- Verify snapshot includes task-list pagination query/response markers (`limit`, `cursor`, `nextCursor`).

**Acceptance criteria**
- `verifyOpenApiSnapshot` passes with strict equality.
- `OpenApiSnapshotContractTest` passes marker assertions.
- CI workflow is green on PR branch.

---

### TKT-P1-G17 — ADR canonicalization cleanup and reference hygiene
**Goal**  
Finish elimination of ADR ambiguity and ensure active references resolve only to canonical files.

**Scope**
- Apply ADR-014 policy consistently in docs and planning notes.
- Ensure superseded ADRs are clearly historical and contain explicit forward pointers.
- Remove any remaining references to superseded ADR variants as active policy.

**Acceptance criteria**
- Exactly one canonical ADR filename is referenced per active contract topic in architecture/product docs.
- Superseded ADRs keep explicit “superseded by” pointers.
- No newly merged planning docs reintroduce duplicate-active ADR references.

---

### TKT-P1-A18 — Cursor evolution readiness (dual-decode strategy + compatibility tests)
**Goal**  
Prepare a safe evolution path from pure offset cursor tokens to future seek-style tokens without REST/MCP breaking changes.

**Scope**
- Implement cursor parser that accepts current offset cursor and an extensible prefixed token format.
- Preserve response envelope names (`tasks`, `nextCursor`) and existing behavior for offset callers.
- Add transport-parity tests for mixed cursor formats and terminal page semantics.
- Document migration constraints in architecture + ADR-015.

**Acceptance criteria**
- Existing offset cursor clients remain fully backward compatible.
- Invalid cursor formats still map to bad request behavior.
- REST and MCP pagination parity tests cover legacy + prefixed token paths.
- Docs explicitly define that cursor field is opaque and format-evolvable.

---

## 4) Recommended execution order
1. **Slice 1:** TKT-P1-G15 (OpenAPI reconciliation + CI confidence)
2. **Slice 2:** TKT-P1-G17 (ADR/source-of-truth cleanup pass)
3. **Slice 3:** TKT-P1-A18 (cursor evolution readiness)

## 5) Active risk register
- OpenAPI drift remains the primary near-term merge/release risk until regenerated on Java 21.
- Lack of local Java runtime in current environment prevents immediate red/green confirmation.
- Large-offset pagination can still degrade over time unless cursor evolution work lands.

## 6) Developer handoff anchor points
- Canonical CI gate: `./gradlew check` (JDK 21).
- Canonical MCP correlation policy: `ADR-012-mcp-correlation-id-canonicalization-policy.md`.
- Canonical pagination ordering policy: `ADR-013-task-list-pagination-ordering-contract.md`.
- Canonical supersession governance: `ADR-014-contract-source-of-truth-and-supersession-policy.md`.
- New cursor evolution strategy: `ADR-015-cursor-token-evolution-and-backward-compatibility.md`.

## 7) Delta captured in this pass
- Rebased planning posture to treat store-level pagination optimization as completed baseline.
- Added forward-looking ticket (TKT-P1-A18) for cursor token evolution compatibility.
- Added ADR-015 to make cursor evolution contract explicit before implementation.
- Refreshed handoff sequence to focus next coding slice on OpenAPI gate recovery first.
