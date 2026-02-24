# PRODUCT_OWNER_NEXT.md

Last updated: 2026-02-24 (PST, OpenAPI pagination-marker hardening + Java21 provenance ladder)
Owner: Product/Architecture

## 1) Current inspection snapshot (this pass)

### Code + tests + docs
- ✅ REST/MCP parity posture remains anchored in shared application services and parity contract tests.
- ✅ Mongo idempotency replay handling includes stale-reference fail-soft behavior with integration coverage.
- ✅ Release-readiness documentation contract test enforces canonical release ADR coverage through ADR-021, including Java-preflight evidence fields.
- ✅ CI workflow remains minimal and release-relevant (`.github/workflows/ci.yml`: JDK 21 + `./gradlew check`).
- ✅ Release lane sequencing, evidence provenance/freshness, and docs-governance policies are explicitly documented (ADR-018/019/020).
- ✅ Cursor compatibility hardening now includes Mongo integration coverage for offset-token forms (`o:<n>`, `O: <n>`) and unsupported-prefix rejection.
- ✅ OpenAPI snapshot markers now explicitly include pagination query parameters and `nextCursor` response field pending Java-21 generated reconciliation.

### Runtime verification
- ⚠️ Local verification blocked in this shell: Java runtime unavailable (`JAVA_HOME`/`java` missing).
- ⚠️ OpenAPI snapshot freshness + final gate status remain CI/Java-enabled-workstation verifiable only.

### Delta introduced in this pass
- Added ADR-021 for Java 21 toolchain readiness and verification provenance ladder policy.
- Tightened release-planning artifacts to require explicit Java preflight declaration (`java -version`, `JAVA_HOME`, evidence source).
- Refined roadmap/backlog/handoff language so lane closure explicitly includes toolchain unblock path before OpenAPI/CI reconciliation.
- Hardened OpenAPI contract checks to assert concrete pagination markers (`query.cursor`, `query.limit`, `nextCursor` + nullable) instead of loose token-only matching, reducing false-green drift risk.

---

## 2) Prioritized roadmap (release confidence first)

### P1 — Lane closure for release candidate
1. **TKT-P1-G21 — Java 21 toolchain preflight + verification source declaration (ADR-021)**
2. **TKT-P1-G15 — OpenAPI snapshot reconciliation + CI green evidence**
3. **TKT-P1-G19 — Evidence provenance/freshness enforcement rollout (ADR-019 + ADR-021)**
4. **TKT-P1-G17 — Canonical ADR reference hygiene final sweep (through ADR-021)**

### P2 — Post-lane hardening (after P1 complete)
4. **TKT-P2-A18 — Cursor phase-2 (seek token) readiness and parity expansion**
5. Authn/authz + tenant boundary contracts
6. Outbox/domain-event baseline and retention policy

---

## 3) Implementation-ready tickets

### TKT-P1-G21 — Java 21 toolchain preflight + verification source declaration
**Goal**: remove execution ambiguity before release-lane verification.

**Scope**
- Record Java preflight in release artifacts:
  - `java -version` outcome
  - `JAVA_HOME` presence/absence
  - declared evidence source (`local-java21` or `ci-java21`)
- If local Java 21 unavailable, explicitly route verification to CI and mark local constraint.

**Acceptance criteria**
- `docs/release-evidence.md` and PR template include Java preflight fields.
- Handoff note explicitly states verification source and any local limitation.
- No GO statement is issued without preflight declaration.

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
**Goal**: make GO/NO-GO decisions auditable, time-bounded, and source-explicit.

**Scope**
- Complete PR template + release evidence fields for ADR-019:
  - evidence source type
  - commit SHA parity
  - freshness window status
- Ensure handoff notes include explicit provenance statement.

**Acceptance criteria**
- PR template contains provenance + freshness checklist items plus Java preflight fields.
- `docs/release-evidence.md` includes <=24h freshness gate, preflight declaration, and owner override note.
- Handoff references ADR-019/021 and records evidence origin.

### TKT-P1-G17 — Canonical ADR reference hygiene final sweep
**Goal**: preserve single-source policy references and keep doc tests authoritative.

**Scope**
- Ensure active docs cite canonical ADR set only.
- Keep superseded ADRs strictly historical with forward pointers.
- Keep docs contract tests synchronized with canonical release ADR set (ADR-020 requirement).

**Acceptance criteria**
- README/ARCHITECTURE/HANDOFF/PRODUCT/ROADMAP are mutually consistent.
- No active policy statement points to superseded ADR files.
- Canonical set references include ADR-021 where release-policy ADRs are listed.

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
1. TKT-P1-G21 (declare Java preflight + verification source)
2. TKT-P1-G15 (run + reconcile + commit snapshot if needed)
3. TKT-P1-G19 (fill provenance/freshness evidence fields)
4. TKT-P1-G17 (final documentation hygiene sweep)
5. TKT-P2-A18 (only after Lane 1 closure)

## 5) Risk register (live)
- **R1 OpenAPI drift unknown** until Java-21 execution evidence exists.
- **R2 Evidence staleness risk** if CI result is not tied to PR head SHA and fresh timestamp.
- **R3 Policy drift risk** if release-policy ADR additions are not synchronized with docs contract tests (ADR-020).
- **R4 Premature scope expansion risk** if ADR-018 lane/feature-freeze is bypassed.
- **R5 Toolchain ambiguity risk** if Java preflight/source declaration is omitted from evidence artifacts (ADR-021).

## 6) Canonical references for active release work
- ADR-012/013/014/015/016/017/018/019/020/021
- `docs/release-evidence.md`
- `.github/pull_request_template.md`
- `docs/rest-mcp-readiness-roadmap.md`
