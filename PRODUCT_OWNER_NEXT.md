# PRODUCT_OWNER_NEXT.md

Last updated: 2026-02-24 (PST, product/architecture continuity pass)
Owner: Product/Architecture

## 1) Latest inspection snapshot (code/docs/tests/CI)

### Repo state validated in this pass
- ✅ REST + MCP parity remains anchored in shared services with explicit parity regression tests (`TaskRestMcpParityTest`).
- ✅ Cursor compatibility baseline is intact (`<n>` and `o:<n>` decode accepted; unsupported token families rejected).
- ✅ MCP correlation canonicalization/fallback remains governed by canonical ADR-012.
- ✅ CI gate still runs `./gradlew check` on JDK 21 (`.github/workflows/ci.yml`).
- ✅ Release-evidence workflow artifacts are now present:
  - `docs/release-evidence.md`
  - `.github/pull_request_template.md`
  - policy captured in `ADR-017-release-evidence-artifact-and-pr-template-policy.md`

### Active risks / unresolved verification
- ⚠️ Local shell lacks Java runtime (`JAVA_HOME`/`java` missing); no in-shell verification run possible.
- ⚠️ OpenAPI snapshot freshness remains unverified in this environment until Java 21 run is completed.

### Verification log (this run)
- Attempted: `./gradlew check`
- Result: failed immediately with `JAVA_HOME is not set and no 'java' command could be found in your PATH`.
- Impact: release evidence must be captured from CI/Java-enabled workstation for TKT-P1-G15.

---

## 2) Prioritized roadmap (REST + MCP readiness)

## P1 — Immediate release confidence
1. **TKT-P1-G15 — OpenAPI snapshot reconciliation + CI green confirmation** *(next coding slice)*
2. **TKT-P1-G17 — ADR canonicalization cleanup and reference hygiene** *(final sweep only)*

## P2 — Short-horizon hardening
3. **TKT-P2-A18 — Cursor evolution phase-2 planning (seek-token emission readiness)**
4. Authn/authz and tenant boundaries
5. Domain event/outbox shape for downstream integrations
6. Archive/retention lifecycle policy

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
- Fill release evidence bundle in PR using `docs/release-evidence.md` fields.

**Acceptance criteria**
- `verifyOpenApiSnapshot` passes.
- `OpenApiSnapshotContractTest` passes.
- CI workflow is green on PR branch.
- PR has complete release evidence section (CI URL/SHA, OpenAPI result, parity statement, canonical ADR references).

### TKT-P1-G17 — ADR canonicalization cleanup and reference hygiene
**Goal**  
Eliminate lingering ADR ambiguity and keep one canonical reference per active contract topic.

**Scope**
- Ensure architecture/product/handoff docs reference only canonical files (ADR-012/013/014/015/016/017).
- Keep superseded ADRs explicitly marked historical with forward pointers.

**Acceptance criteria**
- No planning/architecture doc points to superseded ADR as active policy.
- Canonical ADR list is consistent across README, ARCHITECTURE, PRODUCT_OWNER_NEXT, handoff notes.

### TKT-P2-A18 — Cursor evolution phase-2 planning (seek-token emission readiness)
**Goal**  
Define phase-2 token evolution without changing external fields (`cursor`, `nextCursor`, `limit`).

**Scope**
- Document migration phases and rollback posture.
- Define mixed-token REST/MCP parity test plan.
- Maintain canonical planning artifact at `docs/cursor-evolution-phase2-plan.md`.

**Acceptance criteria**
- Backward compatibility for legacy offset callers is explicit.
- Proposed tests cover terminal-page semantics and malformed token handling.

---

## 4) Recommended execution order
1. **Slice 1:** TKT-P1-G15 (OpenAPI reconciliation + CI confidence)
2. **Slice 2:** TKT-P1-G17 (canonical reference hygiene final sweep)
3. **Slice 3:** TKT-P2-A18 (seek-token phase-2 planning)

## 5) Active risk register
- OpenAPI drift remains primary release risk until Java 21 verification is executed and captured.
- Local runtime gap continues to block in-shell red/green validation.

## 6) Developer handoff anchor points
- Canonical CI gate: `./gradlew check` (JDK 21)
- OpenAPI gate: `verifyOpenApiSnapshot` + `OpenApiSnapshotContractTest`
- Release evidence template: `docs/release-evidence.md`
- Canonical ADRs:
  - `ADR-012-mcp-correlation-id-canonicalization-policy.md`
  - `ADR-013-task-list-pagination-ordering-contract.md`
  - `ADR-014-contract-source-of-truth-and-supersession-policy.md`
  - `ADR-015-cursor-token-evolution-and-backward-compatibility.md`
  - `ADR-016-release-readiness-evidence-and-go-no-go-gate.md`
  - `ADR-017-release-evidence-artifact-and-pr-template-policy.md`

## 7) Delta captured in this pass
- Added ADR-017 to lock release evidence artifact locations and PR template policy.
- Added `docs/release-evidence.md` as canonical evidence schema.
- Added `.github/pull_request_template.md` section aligned with ADR-016/017.
- Updated architecture/README/backlog references to include ADR-017 and evidence artifacts.
- Revalidated runtime limitation: Java 21 unavailable in this shell (`JAVA_HOME` missing).
