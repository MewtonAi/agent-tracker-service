# ATS Product + Architecture Backlog (REST + MCP + Micronaut + Mongo)

Last updated: 2026-02-24 (PST)
Owner: Product/Architecture

## 1) Repo inspection snapshot (this run)

What is already strong:
- REST and MCP both route through shared application services (`TaskCommandService`, `TaskQueryService`).
- MCP runtime and registration contracts are already covered (`TaskMcpRuntimeTransportContractTest`, `TaskMcpToolRegistrationContractTest`).
- REST/MCP parity tests exist and are CI-gated.
- Mongo persistence has optimistic locking, idempotency replay storage, TTL, and startup index bootstrapping.
- OpenAPI drift and docs/release evidence are strongly governed by ADRs and contract tests.

Primary gaps/risk areas discovered:
- API surface completeness is still task-lifecycle-only; no explicit contract for deletion/archive semantics, assignment flows, or richer list filtering/sorting fields.
- Contract mapping logic is duplicated between REST and MCP adapters (example: status parsing), creating parity drift risk over time.
- Micronaut runtime profile strategy is minimal (single `application.yml`), with no explicit dev/test/prod profile guidance for Mongo/idempotency/telemetry behavior.
- Mongo index/migration lifecycle is startup-create only; no explicit migration/versioning manifest, rollout checks, or index-health verification step in release evidence.
- Reliability and observability are centered on idempotency metrics; there is no explicit SLO deck for end-to-end REST + MCP request quality (latency/error budgets/parity health).

---

## 2) Prioritized ticket backlog (small, actionable)

### P0 — next 1-2 implementation slices

1. **ATS-PA-01 — Contract normalization component for REST/MCP input parsing**
   - Focus: REST API consistency + MCP contract readiness
   - Why now: removes parity drift risk from duplicated adapter logic.

2. **ATS-PA-02 — API completeness decision ADR for task end-of-life + assignment semantics**
   - Focus: REST completeness/consistency + MCP contract scope
   - Why now: endpoint/tool roadmap is blocked without explicit product decision.

3. **ATS-PA-03 — Micronaut profile baseline (`local`, `test`, `prod`) with documented config matrix**
   - Focus: architecture + config profiles + DX/reliability
   - Why now: needed before infra-hardening and predictable CI/local behavior.

4. **ATS-PA-04 — Mongo migration/index manifest and startup verification log contract**
   - Focus: Mongo schema/index strategy + migration reliability
   - Why now: current startup index creation lacks explicit versioned migration posture.

5. **ATS-PA-05 — Cross-transport observability baseline (REST + MCP RED metrics + parity signal)**
   - Focus: observability + reliability + release confidence
   - Why now: today’s telemetry is good for idempotency, not full transport SLOs.

### P1 — immediately after P0

6. **ATS-PA-06 — List query contract v1.1 (explicit sort key contract + safe filter extensions)**
7. **ATS-PA-07 — Unified error catalog generator/check to keep OpenAPI + MCP error codes aligned**
8. **ATS-PA-08 — Mongo resilience testing pack (duplicate-key races, stale replay refs, transient write failures)**
9. **ATS-PA-09 — Consumer-facing MCP tool contract examples (happy path + failure taxonomy)**
10. **ATS-PA-10 — Developer experience guardrails (`make`/Gradle task aliases + one-command local verify profile)**

---

## 3) Acceptance criteria + architecture notes for top items

### ATS-PA-01 — Contract normalization component for REST/MCP input parsing

Acceptance criteria:
- Create shared normalization utility/service for status parsing, cursor/limit validation, and idempotency-key text rules.
- REST controller and MCP tools both use the shared component (duplicate parsing code removed from adapters).
- Parity test coverage includes identical bad-input behavior for REST and MCP (`BAD_REQUEST` mapping + message contract where applicable).
- OpenAPI and MCP registration tests remain green with no behavior regression.

Architecture notes:
- Place under `application.contract` (or equivalent) to keep adapters thin.
- Keep transport-agnostic exceptions in application/domain layers; adapter maps only to wire format.

### ATS-PA-02 — API completeness decision ADR for task end-of-life + assignment semantics

Acceptance criteria:
- New ADR defining explicit v1/v1.1 position for:
  - delete vs archive/cancel behavior,
  - assignment/unassignment surface,
  - minimal filter roadmap (`assignee`, `priority`, `type`) and non-goals.
- ADR includes required REST endpoint + MCP tool parity matrix.
- Backlog is updated with follow-up implementation tickets split into <=1 day tasks.

Architecture notes:
- Keep product decision before coding to avoid REST/MCP divergence.
- Prefer additive contracts (non-breaking) and explicit deprecation policy.

### ATS-PA-03 — Micronaut profile baseline (`local`, `test`, `prod`) with config matrix

Acceptance criteria:
- Add `application-local.yml`, `application-test.yml`, `application-prod.yml` with documented ownership of key settings:
  - `task.store`, `mongodb.uri/database`, idempotency TTL, management endpoints exposure, mcp transport settings.
- README/ARCHITECTURE include profile activation commands/examples.
- CI test path explicitly uses deterministic test profile values.

Architecture notes:
- Use profile-specific defaults to reduce accidental prod-like behavior in local runs.
- Keep secrets out of repo; only placeholders + env-var contracts documented.

### ATS-PA-04 — Mongo migration/index manifest and startup verification log contract

Acceptance criteria:
- Add lightweight migration manifest document (versioned list of required indexes + ownership).
- Startup index initializer emits deterministic structured logs for created/existing indexes.
- Add integration test that verifies required indexes exist in Mongo test resources.
- Release evidence template references migration/index verification signal.

Architecture notes:
- Keep current startup-create strategy, but make it auditable and versioned.
- Avoid destructive index operations without explicit manual gate.

### ATS-PA-05 — Cross-transport observability baseline (REST + MCP RED metrics + parity signal)

Acceptance criteria:
- Add metrics for request rate, error count, and duration by transport + operation + normalized outcome code.
- Add a parity health metric/counter for REST-vs-MCP contract mismatch incidents.
- Document metric names and dashboards/alerts in `OBSERVABILITY.md`.
- Include at least one test or smoke assertion that expected meters are registered.

Architecture notes:
- Reuse Micrometer; keep label cardinality bounded (no taskId/idempotency key labels).
- Align outcome labels with existing error `code` catalog.

---

## 4) Suggested execution order

1. ATS-PA-01 (drift prevention and shared contract behavior)
2. ATS-PA-02 (product contract decisions to unlock scope)
3. ATS-PA-03 (profile clarity and runtime discipline)
4. ATS-PA-04 (Mongo migration/index reliability)
5. ATS-PA-05 (end-to-end observability baseline)

---

## 5) Next top 3 developer tickets

1. **ATS-PA-01** — build shared REST/MCP input normalization component and remove adapter duplication.
2. **ATS-PA-03** — introduce Micronaut profile baseline and document config matrix.
3. **ATS-PA-04** — add Mongo index migration manifest + startup verification logging + integration check.
