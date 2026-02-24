# PRODUCT_OWNER_NEXT.md

Last updated: 2026-02-24 (PST, product/architecture backlog refinement + ADR-019 pass)
Owner: Product/Architecture

## 1) Current inspection snapshot (this pass)

### Code + tests + docs
- ✅ REST/MCP parity posture still anchored in shared application services and parity contract tests.
- ✅ Mongo idempotency replay handling is hardened for stale replay references (missing task result documents), with integration coverage.
- ✅ Release-readiness contract tests exist (`ReleaseReadinessDocumentationContractTest`) and enforce canonical ADR hygiene across key docs.
- ✅ CI workflow remains minimal and correct for release confidence (`.github/workflows/ci.yml` -> JDK 21 + `./gradlew check`).
- ✅ ADR lane sequencing for release-candidate readiness is in place (ADR-018).

### Runtime verification
- ⚠️ Local verification still blocked in this shell: Java runtime unavailable (`JAVA_HOME`/`java` missing).
- ⚠️ As a result, OpenAPI snapshot freshness and full gate outcome remain "CI-only verifiable" for this pass.

### Delta introduced in this pass
- Hardened Mongo idempotency replay lookup to fail soft (null replay) when historical replay record points to a missing task, and added integration coverage for this case.
- Added ADR-019 to formalize release-evidence provenance + freshness policy.
- Expanded release-evidence workflow artifacts to include provenance/freshness checks.
- Added implementation-ready roadmap artifact: `docs/rest-mcp-readiness-roadmap.md`.

---

## 2) Prioritized roadmap (release confidence first)

### P1 — Lane closure for release candidate
1. **TKT-P1-G15 — OpenAPI snapshot reconciliation + CI green evidence**
2. **TKT-P1-G19 — Evidence provenance/freshness enforcement rollout (ADR-019)**
3. **TKT-P1-G17 — Canonical ADR reference hygiene final sweep**

### P2 — Post-lane hardening (after P1 complete)
4. **TKT-P2-A18 — Cursor phase-2 (seek token) readiness and parity expansion**
5. Authn/authz + tenant boundary contracts
6. Outbox/domain-event baseline and retention policy

---

## 3) Implementation-ready tickets

### TKT-P1-G15 — OpenAPI snapshot reconciliation + CI green evidence
**Goal**: eliminate contract drift risk before GO/NO-GO.

**Scope**
- Run in Java 21 environment:
  - `./gradlew updateOpenApiSnapshot`
  - `./gradlew check`
- Commit `openapi/openapi.yaml` if regenerated.
- Populate release evidence in PR/handoff via `docs/release-evidence.md`.

**Acceptance criteria**
- `verifyOpenApiSnapshot` passes.
- `OpenApiSnapshotContractTest` passes.
- CI run for the PR head SHA is green.
- Evidence captures CI URL + SHA + OpenAPI diff outcome.

### TKT-P1-G19 — Evidence provenance/freshness enforcement rollout
**Goal**: make GO/NO-GO decisions auditable and time-bounded.

**Scope**
- Align PR template + release evidence doc with ADR-019 fields:
  - evidence source type
  - commit SHA parity
  - freshness window status
- Ensure handoff notes include explicit provenance statement.

**Acceptance criteria**
- PR template contains provenance + freshness checklist items.
- `docs/release-evidence.md` includes <=24h freshness gate and owner override note.
- Handoff references ADR-019 and records evidence origin.

### TKT-P1-G17 — Canonical ADR reference hygiene final sweep
**Goal**: preserve single-source policy references.

**Scope**
- Ensure active docs cite canonical ADR set only.
- Keep superseded ADRs strictly historical with forward pointers.

**Acceptance criteria**
- README/ARCHITECTURE/HANDOFF/PRODUCT docs are mutually consistent.
- No active policy statement points to superseded ADR files.

### TKT-P2-A18 — Cursor phase-2 readiness
**Goal**: phase seek-token evolution without wire-contract changes.

**Scope**
- Keep `docs/cursor-evolution-phase2-plan.md` current.
- Define dual-mode parity test matrix (`offset`, `seek`).
- Capture rollback owner and rollback-switch posture.

**Acceptance criteria**
- Mixed-token scenarios are explicitly listed and testable.
- Terminal-page and malformed-token behavior remains parity-aligned.
- OpenAPI field shape remains unchanged (`cursor`, `nextCursor`, `limit`).

---

## 4) Execution order for next coding slice
1. TKT-P1-G15 (run + reconcile + commit snapshot if needed)
2. TKT-P1-G19 (fill provenance/freshness evidence fields)
3. TKT-P1-G17 (final documentation hygiene sweep)
4. TKT-P2-A18 (only after Lane 1 closure)

## 5) Risk register (live)
- **R1 OpenAPI drift unknown** until Java-21 execution evidence exists.
- **R2 Evidence staleness risk** if CI result is not tied to PR head SHA and fresh timestamp.
- **R3 Premature scope expansion risk** if ADR-018 lane/feature-freeze is bypassed.

## 6) Canonical references for active release work
- ADR-012/013/014/015/016/017/018/019
- `docs/release-evidence.md`
- `.github/pull_request_template.md`
- `docs/rest-mcp-readiness-roadmap.md`
