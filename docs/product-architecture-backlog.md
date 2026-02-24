# ATS Product + Architecture Backlog (REST + MCP + Micronaut + Mongo)

Last updated: 2026-02-24 (PST, execution-ready refinement)
Owner: Product/Architecture

## Repo inspection snapshot (this run)

Confirmed strengths:
- Shared application services already back both transports (`TaskCommandService`, `TaskQueryService`).
- MCP registration + runtime handshake contracts are tested (`TaskMcpToolRegistrationContractTest`, `TaskMcpRuntimeTransportContractTest`).
- REST/MCP parity tests exist (`TaskRestMcpParityTest`).
- OpenAPI drift gate and release-policy ADR/test posture are strong.
- Mongo baseline includes optimistic locking, idempotency uniqueness/hash checks, and TTL.

Current product/architecture gaps to close next:
1. Input/validation logic is still partially duplicated in transport adapters (future drift risk).
2. API completeness decisions (task archive/delete, assignment semantics, filter roadmap) are not canonically locked for implementation.
3. Micronaut profile strategy is not explicit (`local/test/prod` ownership and defaults missing).
4. Mongo index lifecycle is startup-create only; missing versioned manifest + explicit verification evidence.
5. Observability is idempotency-centric; missing transport-wide RED + parity mismatch signal.

---

## Prioritized backlog

## P0 (do now)

### ATS-PA-01 — Shared contract normalization for REST + MCP adapters
**Goal**: eliminate input contract drift across transports.

**Scope**:
- Introduce shared normalization component for:
  - `TaskStatus` parsing/validation,
  - `limit/cursor` bounds checks,
  - idempotency key textual validation rules.
- Migrate `TaskController` + `TaskMcpTools` to shared component.

**Acceptance criteria**:
- No direct status/limit/idempotency parsing remains inside transport adapters.
- REST and MCP return equivalent invalid-input semantics for shared inputs.
- New/updated tests cover both transports for at least:
  - invalid status,
  - invalid limit,
  - blank/invalid idempotency key.
- Existing parity and contract suites remain green.

**Architecture notes**:
- Place in `application.contract` package (transport-agnostic).
- Keep wire-specific error mapping in adapter layer only.

**Dependencies**: none.

---

### ATS-PA-02 — ADR: API completeness boundary (archive/delete + assignment)
**Goal**: lock product behavior before adding new REST/MCP surfaces.

**Scope**:
- New ADR that decides:
  - hard delete vs archive/cancel contract,
  - assignment/unassignment semantics,
  - v1.1 filter additions (`assignee`, `priority`, `type`) and explicit non-goals.
- Include parity matrix (REST endpoint ↔ MCP tool).

**Acceptance criteria**:
- ADR has a single canonical decision and migration posture (additive/non-breaking).
- Follow-up implementation work is split into <=1 day tickets.
- README + roadmap/backlog reference the new ADR as source of truth.

**Architecture notes**:
- Preserve current task-first bounded context.
- Prefer additive APIs; avoid route/tool removals in v1.x.

**Dependencies**: none (unblocks ATS-PA-06 and future assignment work).

---

### ATS-PA-03 — Micronaut profile baseline + config matrix
**Goal**: deterministic runtime behavior across local/test/prod.

**Scope**:
- Add `application-local.yml`, `application-test.yml`, `application-prod.yml`.
- Define ownership for keys:
  - `task.store`, `mongodb.*`, `idempotency.ttl-hours`, management endpoint exposure, MCP transport toggles.
- Document activation commands in README/ARCHITECTURE.

**Acceptance criteria**:
- Profile files exist with non-secret defaults/placeholders only.
- CI tests run with explicit deterministic `test` profile.
- Local developer run path is documented in one command sequence.

**Architecture notes**:
- Keep secrets externalized through env vars.
- Use stricter prod defaults than local/test.

**Dependencies**: none.

---

### ATS-PA-04 — Mongo index/migration manifest + verification contract
**Goal**: make index lifecycle auditable and release-verifiable.

**Scope**:
- Add `docs/mongo-index-manifest.md` with versioned required indexes.
- Define deterministic startup log/event for index created/existing outcomes.
- Add integration test asserting required indexes exist in Mongo-backed tests.
- Link verification signal in `docs/release-evidence.md`.

**Acceptance criteria**:
- Manifest lists all required indexes and ownership.
- Startup emits verifiable structured index state messages.
- Automated test fails when required index is missing/changed unexpectedly.
- Release evidence includes index verification result.

**Architecture notes**:
- Keep non-destructive startup creation; destructive changes require explicit manual gate ADR/ticket.

**Dependencies**: ATS-PA-03 recommended first (profile determinism for Mongo tests).

---

### ATS-PA-05 — Cross-transport observability baseline (RED + parity)
**Goal**: measure REST and MCP service quality with bounded-cardinality metrics.

**Scope**:
- Add request rate/error/duration metrics by `{transport,operation,outcome}`.
- Add parity mismatch counter for REST-vs-MCP contract discrepancies.
- Update `OBSERVABILITY.md` with metric names and alert suggestions.

**Acceptance criteria**:
- Metrics are emitted for both REST and MCP code paths.
- Labels exclude high-cardinality identifiers (`taskId`, idempotency keys, correlation IDs).
- At least one automated assertion validates meter registration.

**Architecture notes**:
- Reuse Micrometer registry and existing error-code vocabulary for `outcome`.

**Dependencies**: ATS-PA-01 (shared normalization improves outcome consistency).

---

## P1 (next)

6. **ATS-PA-06** — List query contract v1.1 (sort/filter extension under ADR from ATS-PA-02).
7. **ATS-PA-07** — Error catalog single-source generator/check for OpenAPI + MCP parity.
8. **ATS-PA-08** — Mongo resilience test pack (duplicate key races, transient failure retries, stale replay scenarios).
9. **ATS-PA-09** — MCP consumer examples pack (success/failure contracts with copy-paste JSON).
10. **ATS-PA-10** — DX guardrails (`verifyLocal` aggregate Gradle task + profile-aware quickstart).

---

## Recommended execution order
1. ATS-PA-01
2. ATS-PA-03
3. ATS-PA-04
4. ATS-PA-05
5. ATS-PA-02 (or parallel as ADR track), then ATS-PA-06

---

## Definition of Done (for all ATS-PA tickets)
- Tests added/updated and passing in `./gradlew check`.
- No REST/MCP contract divergence introduced (parity tests green).
- Relevant docs updated (README/ARCHITECTURE/OBSERVABILITY/release evidence as applicable).
- Ticket artifact includes rollback note when behavior/config changes.

---

## Next top 3 developer tickets
1. **ATS-PA-01** — Shared normalization component for REST/MCP adapters.
2. **ATS-PA-03** — Micronaut profile baseline and config matrix.
3. **ATS-PA-04** — Mongo index manifest + startup verification + integration assertion.
