# ATS Product + Architecture Backlog (REST + MCP + Micronaut + Mongo)

Last updated: 2026-02-24 (PST, repo re-inspection + re-prioritization)
Owner: Product/Architecture

## Repo inspection snapshot (this run)

### Confirmed implemented
- Shared task application services back REST + MCP (`TaskCommandService`, `TaskQueryService`).
- Shared transport-agnostic input normalization exists and is used by both adapters (`application.contract.TaskInputNormalizer`).
- MCP contract gates exist at registration and runtime handshake layers.
- REST/MCP parity tests exist and are part of `check`.
- Profile files exist (`application-local.yml`, `application-test.yml`, `application-prod.yml`) and matrix doc exists.
- Mongo baseline exists: optimistic locking, idempotency uniqueness/hash mismatch, TTL index creation.

### Highest-value remaining gaps
1. API completeness decisions are not canonically locked (archive/delete/assignment/filter roadmap).
2. Mongo index lifecycle is not yet auditable as a versioned manifest + explicit verification signal.
3. Observability remains idempotency-centric; missing cross-transport RED baseline and parity-drift signal.
4. Error-catalog governance is still primarily test assertions, not a single generated source shared by REST/OpenAPI/MCP.
5. Developer UX for local verification is fragmented (no single profile-aware guardrail task).

---

## Prioritized backlog (small, execution-ready)

## P0 (do now)

### ATS-PA-02 — ADR: API completeness boundary (archive/delete + assignment + list filter scope)
**Why now**: prevents scope churn and contract churn before adding new endpoints/tools.

**Implementation slices (<= 1 day each)**
- 02a: Draft decision options and recommendation with compatibility analysis.
- 02b: Final ADR with parity matrix (REST route ↔ MCP tool) and migration posture.
- 02c: Propagate canonical references into README + roadmap + backlog.

**Acceptance criteria**
- ADR explicitly decides:
  - hard delete vs archive/cancel semantics,
  - assignment/unassignment semantics,
  - v1.1 list filter scope (`assignee`, `priority`, `type`) and non-goals.
- Decision documents request/response compatibility posture as additive/non-breaking for v1.x.
- ADR includes parity mapping table for each accepted capability across REST + MCP.
- Follow-up implementation work is split into concrete tickets each scoped to <=1 dev day.

**Architecture notes**
- Keep task-first bounded context; no project-routable APIs introduced.
- Prefer additive contracts; no field removals/renames in v1.x.
- Keep domain language canonical (`CANCELED` remains lifecycle state; avoid introducing duplicate archival state unless intentionally modeled).

**Dependencies**: none.

---

### ATS-PA-04 — Mongo index/migration manifest + startup verification contract
**Why now**: turns current implicit startup behavior into auditable release evidence.

**Implementation slices (<= 1 day each)**
- 04a: Add `docs/mongo-index-manifest.md` v1 with index purpose/ownership/change policy.
- 04b: Emit structured startup logs for index create/exist outcomes.
- 04c: Add integration assertion for required index set in mongo-backed tests.
- 04d: Add release-evidence checklist line item + reference.

**Acceptance criteria**
- Manifest lists required indexes including key order, uniqueness/TTL options, owning component, and rationale.
- Startup emits deterministic structured events for each required index with status `created|already_exists`.
- Automated test fails when required index spec diverges from manifest/initializer contract.
- Release evidence artifact references and records index verification signal.

**Architecture notes**
- Non-destructive index creation only at startup.
- Destructive index changes (drop/rename/change uniqueness) require explicit ADR + manual migration runbook.
- Keep index naming deterministic to support assertion + operations runbooks.

**Dependencies**: profile determinism already available; no hard blockers.

---

### ATS-PA-05 — Cross-transport observability baseline (REST + MCP RED + parity mismatch)
**Why now**: enables objective reliability tracking beyond idempotency internals.

**Implementation slices (<= 1 day each)**
- 05a: Add shared telemetry seam for transport/operation/outcome counters and duration timer.
- 05b: Instrument REST controller paths and MCP tool entrypoints.
- 05c: Add parity mismatch counter for contract-level mapping mismatches.
- 05d: Extend `OBSERVABILITY.md` with metric dictionary and starter alerts.

**Acceptance criteria**
- Metrics emitted for both transports with dimensions `{transport,operation,outcome}`.
- Bounded-cardinality rule enforced (no task/idempotency/correlation identifiers in labels).
- At least one automated test asserts meter registration and tag set.
- Documentation includes metric names, semantics, and baseline alert suggestions.

**Architecture notes**
- Keep instrumentation centralized (application/telemetry seam), not duplicated in adapters.
- Reuse canonical error code vocabulary for `outcome` where applicable.

**Dependencies**: none.

---

### ATS-PA-11 — Shared correlation-id normalization policy component (REST + MCP)
**Why now**: MCP has inline normalization logic; centralizing avoids future drift with REST policy ADRs.

**Implementation slices (<= 1 day each)**
- 11a: Move UUID validation/fallback logic from `TaskMcpTools` into shared contract component.
- 11b: Apply component in MCP path and validate consistency with REST correlation behavior.
- 11c: Add parity/error tests for malformed/blank correlation IDs.

**Acceptance criteria**
- No inline correlation-id normalization logic remains in MCP adapter.
- Shared component enforces canonical UUID policy and fallback generation.
- REST/MCP documentation reflects a single normalization contract source.

**Architecture notes**
- Keep transport-specific extraction at adapter edge; normalization in shared contract layer.
- Preserve existing external behavior unless ADR update explicitly approves change.

**Dependencies**: none.

---

## P1 (next)

### ATS-PA-07 — Unified error catalog source (generate/check OpenAPI + MCP mapping)
- Replace duplicated literals with single catalog artifact + generation/check path.
- AC: OpenAPI + exception mapping + MCP mapping derive from one source; CI fails on divergence.

### ATS-PA-08 — Mongo resilience pack (race/retry/failure modes)
- Add tests for duplicate-key races, transient write failures, and stale replay handling.
- AC: deterministic test suite with documented retry/backoff contract.

### ATS-PA-06 — List query contract v1.1 implementation (post ATS-PA-02 ADR)
- Implement selected filters/sort constraints from ATS-PA-02 only.
- AC: OpenAPI + MCP schema + parity tests updated together.

### ATS-PA-09 — MCP consumer examples pack
- Provide copy-paste JSON examples for success and canonical failures.
- AC: examples validated in CI smoke or contract tests.

### ATS-PA-10 — DX local verification guardrails
- Add `verifyLocal` aggregate task (profile-aware) and one-command quickstart path.
- AC: documented command and deterministic pass/fail behavior on fresh clone.

---

## Recommended execution order
1. ATS-PA-02
2. ATS-PA-04
3. ATS-PA-05
4. ATS-PA-11
5. ATS-PA-07
6. ATS-PA-08
7. ATS-PA-06

---

## Definition of Done (all ATS-PA tickets)
- Tests added/updated and passing in `./gradlew check` (or explicitly documented CI fallback with rationale).
- No REST/MCP contract divergence introduced (parity/contract suites green).
- Relevant docs updated (README/ARCHITECTURE/OBSERVABILITY/release evidence/backlog as applicable).
- Ticket artifact contains rollback/mitigation note for behavior, schema, or config changes.
- Ownership + evidence source (local-java21 or ci-java21) recorded for release-significant work.

---

## Next top 3 developer tickets
1. **ATS-PA-02** — Lock API completeness boundary via ADR before adding surfaces.
2. **ATS-PA-04** — Mongo index manifest + startup verification + integration assertion.
3. **ATS-PA-05** — Cross-transport RED observability baseline and parity mismatch signal.
