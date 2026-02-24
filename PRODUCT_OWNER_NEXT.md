# PRODUCT_OWNER_NEXT.md

Last updated: 2026-02-24 (PST, product/architecture continuity pass)
Owner: Product/Architecture

## 1) Latest inspection snapshot (code/docs/tests/CI)

### Repo state validated in this pass
- ✅ REST + MCP parity remains anchored in shared services with explicit parity regression tests (`TaskRestMcpParityTest`).
- ✅ Pagination evolution baseline is in place:
  - store seam: `TaskStore#listTasksPage`
  - deterministic ordering: `updatedAt DESC, taskId DESC` (ADR-013 canonical)
  - dual-decode cursor compatibility in `TaskQueryService` (`<n>` and `o:<n>`) per ADR-015
- ✅ MCP correlation canonicalization/fallback is implemented and documented against canonical ADR-012.
- ✅ CI gate remains intentionally simple (`.github/workflows/ci.yml` runs `./gradlew check` on JDK 21).

### Active risks / unresolved verification
- ⚠️ Local shell still lacks Java runtime (`java` not found), so no in-shell test re-run was possible.
- ⚠️ OpenAPI snapshot may still be stale until regenerated/verified on Java 21 (`updateOpenApiSnapshot` + `check`).
- ⚠️ Release evidence is still process-fragile unless standardized per PR; this run adds ADR-016 to lock policy.

---

## 2) Prioritized roadmap (REST + MCP readiness)

## P1 — Immediate release confidence

### EPIC P1-G: Contract governance + deterministic release gates
1. **TKT-P1-G15 — OpenAPI snapshot reconciliation + CI green confirmation** *(next coding slice)*
2. **TKT-P1-G19 — Release evidence bundle wiring (ADR-016) for PR/handoff flow**
3. **TKT-P1-G17 — ADR canonicalization cleanup and reference hygiene**

## P2 — Short-horizon platform hardening
4. **TKT-P2-A18 — Cursor evolution phase-2 planning (seek-token emission readiness, no API field breakage)**
5. Authn/authz and tenant boundaries
6. Domain event/outbox shape for downstream integrations
7. Archive/retention lifecycle policy

---

## 3) Implementation-ready tickets (acceptance-criteria grade)

### TKT-P1-G15 — OpenAPI snapshot reconciliation + CI green confirmation
**Goal**  
Restore strict OpenAPI gate alignment with currently shipped REST contract.

**Scope**
- Run on Java 21 environment:
  - `./gradlew updateOpenApiSnapshot`
  - `./gradlew check`
- Commit regenerated `openapi/openapi.yaml` if changed.
- Verify snapshot contains task-list pagination markers (`limit`, `cursor`, `nextCursor`).

**Acceptance criteria**
- `verifyOpenApiSnapshot` passes with strict equality.
- `OpenApiSnapshotContractTest` passes marker assertions.
- CI workflow is green on PR branch.

---

### TKT-P1-G19 — Release evidence bundle wiring (ADR-016)
**Goal**  
Make release go/no-go deterministic and auditable by standardizing required verification evidence.

**Scope**
- Add repository-level checklist artifact (PR template checklist section or dedicated `docs/release-evidence.md` reference).
- Ensure evidence requires:
  - CI `./gradlew check` pass on JDK 21 (link + SHA)
  - OpenAPI snapshot verification outcome and snapshot diff status
  - statement that parity tests were included via default `check` gate
  - canonical ADR references only (ADR-012/013/014/015/016)
- Update handoff template/notes to mirror the same evidence fields.

**Acceptance criteria**
- A contributor can fill one deterministic evidence bundle without ad-hoc interpretation.
- Handoff notes and PR checklist use the same required evidence headings.
- No release PR can claim readiness without explicit OpenAPI/CI/parity evidence.

---

### TKT-P1-G17 — ADR canonicalization cleanup and reference hygiene
**Goal**  
Finish elimination of ADR ambiguity and ensure active references resolve only to canonical files.

**Scope**
- Apply ADR-014 policy consistently in docs/planning notes.
- Keep superseded ADRs explicitly historical with forward pointers.
- Prevent reintroduction of duplicate-active references.

**Acceptance criteria**
- Exactly one canonical ADR filename is referenced per active contract topic in architecture/product docs.
- Superseded ADRs include explicit “superseded by” pointers.
- New planning docs do not reintroduce duplicate-active ADR references.

---

### TKT-P2-A18 — Cursor evolution phase-2 planning (seek-token emission readiness)
**Goal**  
Design implementation slice for eventual seek-token emission while preserving current external contract.

**Scope**
- Keep request/response fields unchanged (`cursor`, `nextCursor`, `limit`).
- Define phased emission strategy (offset-only -> dual-emit acceptance -> seek emit) without breaking existing clients.
- Add test-plan outline for REST/MCP parity under mixed token families.

**Acceptance criteria**
- Planning doc identifies migration phases and rollback posture.
- Backward compatibility for legacy offset callers is explicitly preserved.
- Proposed tests cover terminal-page semantics and malformed token handling.

---

## 4) Recommended execution order
1. **Slice 1:** TKT-P1-G15 (OpenAPI reconciliation + CI confidence)
2. **Slice 2:** TKT-P1-G19 (release evidence bundle wiring)
3. **Slice 3:** TKT-P1-G17 (ADR/source-of-truth cleanup)
4. **Slice 4:** TKT-P2-A18 (seek-token phase-2 planning)

## 5) Active risk register
- OpenAPI drift remains the primary near-term merge/release risk until regenerated on Java 21.
- No local Java runtime in this shell continues to block direct red/green verification.
- Release readiness can still be interpreted inconsistently until ADR-016 evidence bundle is wired into contributor workflow.

## 6) Developer handoff anchor points
- Canonical CI gate: `./gradlew check` (JDK 21)
- OpenAPI gate: `verifyOpenApiSnapshot` + `OpenApiSnapshotContractTest`
- Canonical ADRs:
  - `ADR-012-mcp-correlation-id-canonicalization-policy.md`
  - `ADR-013-task-list-pagination-ordering-contract.md`
  - `ADR-014-contract-source-of-truth-and-supersession-policy.md`
  - `ADR-015-cursor-token-evolution-and-backward-compatibility.md`
  - `ADR-016-release-readiness-evidence-and-go-no-go-gate.md`

## 7) Delta captured in this pass
- Added ADR-016 to formalize release evidence and deterministic go/no-go policy.
- Updated architecture doc to include ADR-016 governance and focus area.
- Re-prioritized backlog to insert release-evidence wiring directly after OpenAPI reconciliation.
- Refreshed ticket acceptance criteria to be implementation-ready for next coding slice.
- Hardened cursor token decode reliability (`<n>` + `o:<n>`) with explicit unsupported-prefix rejection and whitespace/case tolerance.
- Extended cursor compatibility coverage across application, REST controller, and REST/MCP parity tests.
