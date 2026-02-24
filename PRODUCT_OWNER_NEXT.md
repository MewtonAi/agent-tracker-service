# PRODUCT_OWNER_NEXT.md

Last updated: 2026-02-24 (PST, product/architecture backlog refinement + ADR-020 docs-contract coverage)
Owner: Product/Architecture

## 1) Current inspection snapshot (this pass)

### Code + tests + docs
- ✅ REST/MCP parity posture remains anchored in shared application services and parity contract tests.
- ✅ Mongo idempotency replay handling includes stale-reference fail-soft behavior with integration coverage.
- ✅ Release-readiness documentation contract test now expanded to enforce canonical release ADR coverage through ADR-020.
- ✅ CI workflow remains minimal and release-relevant (`.github/workflows/ci.yml`: JDK 21 + `./gradlew check`).
- ✅ Release lane sequencing, evidence provenance/freshness, and docs-governance policies are explicitly documented (ADR-018/019/020).

### Runtime verification
- ⚠️ Local verification blocked in this shell: Java runtime unavailable (`JAVA_HOME`/`java` missing).
- ⚠️ OpenAPI snapshot freshness + final gate status remain CI/Java-enabled-workstation verifiable only.

### Delta introduced in this pass
- Added ADR-020 for release-contract documentation coverage policy.
- Upgraded `ReleaseReadinessDocumentationContractTest` to include:
  - canonical ADR list through ADR-020,
  - planning artifact checks,
  - ADR-019 provenance/freshness assertions.
- Refined roadmap/backlog/handoff language to include ADR-020 governance.

---

## 2) Prioritized roadmap (release confidence first)

### P1 — Lane closure for release candidate
1. **TKT-P1-G15 — OpenAPI snapshot reconciliation + CI green evidence**
2. **TKT-P1-G19 — Evidence provenance/freshness enforcement rollout (ADR-019)**
3. **TKT-P1-G17 — Canonical ADR reference hygiene final sweep (through ADR-020)**

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
- CI run for PR head SHA is green.
- Evidence captures CI URL + SHA + OpenAPI diff outcome.

### TKT-P1-G19 — Evidence provenance/freshness enforcement rollout
**Goal**: make GO/NO-GO decisions auditable and time-bounded.

**Scope**
- Complete PR template + release evidence fields for ADR-019:
  - evidence source type
  - commit SHA parity
  - freshness window status
- Ensure handoff notes include explicit provenance statement.

**Acceptance criteria**
- PR template contains provenance + freshness checklist items.
- `docs/release-evidence.md` includes <=24h freshness gate and owner override note.
- Handoff references ADR-019 and records evidence origin.

### TKT-P1-G17 — Canonical ADR reference hygiene final sweep
**Goal**: preserve single-source policy references and keep doc tests authoritative.

**Scope**
- Ensure active docs cite canonical ADR set only.
- Keep superseded ADRs strictly historical with forward pointers.
- Keep docs contract tests synchronized with canonical release ADR set (ADR-020 requirement).

**Acceptance criteria**
- README/ARCHITECTURE/HANDOFF/PRODUCT/ROADMAP are mutually consistent.
- No active policy statement points to superseded ADR files.
- Canonical set references include ADR-020 where release-policy ADRs are listed.

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
- **R3 Policy drift risk** if release-policy ADR additions are not synchronized with docs contract tests (ADR-020).
- **R4 Premature scope expansion risk** if ADR-018 lane/feature-freeze is bypassed.

## 6) Canonical references for active release work
- ADR-012/013/014/015/016/017/018/019/020
- `docs/release-evidence.md`
- `.github/pull_request_template.md`
- `docs/rest-mcp-readiness-roadmap.md`
