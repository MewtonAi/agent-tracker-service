# PRODUCT_OWNER_NEXT.md

Last updated: 2026-02-24 (PST, ADR-023 signal-status normalization + NO-GO default)
Owner: Product/Architecture

## 1) Current inspection snapshot (this pass)

### Code + tests + docs
- ✅ REST/MCP parity posture remains anchored in shared application services and parity contract tests.
- ✅ Mongo idempotency replay handling includes stale-reference fail-soft behavior with integration coverage.
- ✅ Release docs contract tests already gate canonical ADR references and release evidence template expectations.
- ✅ ADR-022 minimum release signal declaration remains in force.
- ✅ ADR-023 now tightens release evidence with normalized signal statuses (`PASS`/`FAIL`/`NOT_RUN`) and a deterministic NO-GO default.
- ✅ CI workflow remains minimal and release-relevant (`.github/workflows/ci.yml`: JDK 21 + `./gradlew check`).
- ✅ OpenAPI contract test signal is now more resilient to formatting-only snapshot changes by asserting YAML structure for pagination fields (`cursor`, `limit`, `nextCursor`).

### Runtime verification
- ⚠️ Local verification blocked in this shell: Java runtime unavailable (`JAVA_HOME`/`java` missing).
- ⚠️ OpenAPI reconciliation + final gate remains CI/Java-enabled-workstation verifiable only.

### Delta introduced in this pass
- Added ADR-023 (`ADR-023-release-evidence-signal-status-normalization-and-no-go-default.md`).
- Updated release artifacts (`docs/release-evidence.md`, `.github/pull_request_template.md`) to require normalized status vocabulary and explicit NO-GO default behavior.
- Refined roadmap + backlog sequencing to include implementation-ready signal-normalization rollout ticket before final doc hygiene closure.
- Updated architecture/readme/handoff references to include ADR-023 in canonical release policy set.
- Hardened `OpenApiSnapshotContractTest` with YAML-structural assertions for list pagination contract fields and added explicit `testImplementation("org.yaml:snakeyaml")` for deterministic parsing.

---

## 2) Prioritized roadmap (release confidence first)

### P1 — Lane closure for release candidate
1. **TKT-P1-G21 — Java 21 toolchain preflight + verification source declaration (ADR-021)**
2. **TKT-P1-G15 — OpenAPI snapshot reconciliation + CI green evidence**
3. **TKT-P1-G22 — Minimum release test-signal declaration rollout (ADR-022)**
4. **TKT-P1-G23 — Signal-status normalization + NO-GO default rollout (ADR-023)**
5. **TKT-P1-G19 — Evidence provenance/freshness enforcement rollout (ADR-019 + ADR-021)**
6. **TKT-P1-G17 — Canonical ADR reference hygiene final sweep (through ADR-023)**

### P2 — Post-lane hardening
1. **TKT-P2-A18 — Cursor phase-2 (seek token) readiness and parity expansion**
2. Authn/authz + tenant boundary contracts
3. Outbox/domain-event baseline and retention policy

---

## 3) Implementation-ready tickets

### TKT-P1-G21 — Java 21 toolchain preflight + verification source declaration
**Goal**: remove execution ambiguity before release-lane verification.

**Acceptance criteria**
- `docs/release-evidence.md` + PR template include: `java -version`, `JAVA_HOME`, evidence source (`local-java21` / `ci-java21`).
- Handoff explicitly states verification source and local constraints.
- No GO statement issued without preflight declaration.

### TKT-P1-G15 — OpenAPI snapshot reconciliation + CI green evidence
**Goal**: eliminate contract drift risk before GO/NO-GO.

**Acceptance criteria**
- Java 21 execution evidence for `./gradlew updateOpenApiSnapshot` and `./gradlew check`.
- `verifyOpenApiSnapshot` and `OpenApiSnapshotContractTest` pass.
- CI run for PR head SHA is green and linked in evidence bundle.

### TKT-P1-G22 — Minimum release signal declaration rollout
**Goal**: keep release claims auditable at required signal level.

**Acceptance criteria**
- Required signal set remains declared: `TaskRestMcpParityTest`, `TaskMcpRuntimeTransportContractTest`, `OpenApiSnapshotContractTest`, `ReleaseReadinessDocumentationContractTest`.
- Signal source reference (CI URL or local Java21 logs) is recorded for each release claim.
- Missing signal declaration blocks GO.

### TKT-P1-G23 — Signal-status normalization + NO-GO default rollout
**Goal**: remove ambiguous signal wording and enforce deterministic release decision behavior.

**Acceptance criteria**
- All required signals use only `PASS` / `FAIL` / `NOT_RUN` status values.
- Any `FAIL`/`NOT_RUN` required signal forces release decision to remain `NO-GO`.
- Docs contract tests assert ADR-023 references + normalized status markers in release artifacts.

### TKT-P1-G19 — Evidence provenance/freshness enforcement rollout
**Goal**: make GO/NO-GO decisions source-explicit and time-bounded.

**Acceptance criteria**
- Evidence source + SHA parity + <=24h freshness fields are completed.
- Decision owner/timestamp + exception owner (if any) are explicit.
- Handoff references ADR-019/021/023 and evidence origin.

### TKT-P1-G17 — Canonical ADR reference hygiene final sweep
**Goal**: preserve single-source policy references and keep doc tests authoritative.

**Acceptance criteria**
- README/ARCHITECTURE/HANDOFF/PRODUCT/ROADMAP use canonical ADR set through ADR-023.
- No active policy statement points to superseded ADR files.
- Documentation contract tests and planning artifacts remain synchronized.

### TKT-P2-A18 — Cursor phase-2 readiness
**Goal**: phase seek-token evolution without wire-contract changes.

**Acceptance criteria**
- Mixed-token scenarios are explicitly listed and testable.
- Terminal-page and malformed-token behavior remains parity-aligned.
- OpenAPI field shape remains unchanged (`cursor`, `nextCursor`, `limit`).

---

## 4) Execution order for next coding slice
1. TKT-P1-G21
2. TKT-P1-G15
3. TKT-P1-G22
4. TKT-P1-G23
5. TKT-P1-G19
6. TKT-P1-G17
7. TKT-P2-A18 (only after Lane 1 closure)

## 5) Risk register (live)
- **R1 OpenAPI drift unknown** until Java-21 execution evidence exists.
- **R2 Evidence staleness risk** if CI result is not tied to PR head SHA and fresh timestamp.
- **R3 Policy drift risk** if release-policy ADR additions are not synchronized with docs contract tests.
- **R4 Premature scope expansion risk** if ADR-018 lane/feature-freeze is bypassed.
- **R5 Toolchain ambiguity risk** if Java preflight/source declaration is omitted.
- **R6 Signal ambiguity risk** if required signals are not declared per ADR-022.
- **R7 Decision ambiguity risk** if normalized status semantics from ADR-023 are not enforced.

## 6) Canonical references for active release work
- ADR-012/013/014/015/016/017/018/019/020/021/022/023
- `docs/release-evidence.md`
- `.github/pull_request_template.md`
- `docs/rest-mcp-readiness-roadmap.md`
