# ATS Product + Architecture Backlog (REST + MCP + Micronaut + Mongo)

Last updated: 2026-02-24 (PST, continuous product/architecture pass)
Owner: Product/Architecture

## Repo state snapshot (this run)

### Confirmed in place
- REST + MCP adapters already share application services (`TaskCommandService`, `TaskQueryService`).
- Transport-agnostic request normalization exists (`application.contract.TaskInputNormalizer`).
- MCP registration/runtime contract tests exist.
- OpenAPI snapshot verification is wired and CI-gated.
- Profile baseline exists (`local`, `test`, `prod`) with matrix doc.
- Mongo baseline exists: optimistic locking, idempotency hash mismatch guard, TTL cleanup, startup index checks.

### Remaining strategic gaps
1. API boundary for next task capabilities (archive/delete/assignment/filter set) is not yet locked as a canonical decision artifact.
2. MCP tool contract governance is mostly test-driven; examples/compatibility docs for external consumers are thin.
3. Micronaut modular boundaries and profile safety checks are not enforced by dedicated architectural tests.
4. Mongo change-management posture needs explicit migration/runbook contract for destructive index/schema operations.
5. Observability/test strategy still under-specifies cross-transport SLO signals and local developer verification path.

---

## Prioritized backlog

## P0 (execute now)

### ATS-PA-24 — ADR: v1.1 API completeness boundary (archive/delete, assignment, list-filter scope)
**Problem**: team can add endpoints/tools inconsistently unless capability scope is explicitly decided first.

**Scope slices (<=1 day each)**
- 24a: Draft options + recommendation (hard delete vs archive/cancel, assignment semantics, list-filter set).
- 24b: Publish ADR with REST route ↔ MCP tool parity table and compatibility notes.
- 24c: Cascade references in README, architecture doc, roadmap, and backlog.

**Acceptance criteria**
- ADR explicitly decides: delete/archive posture, assignment/unassignment semantics, and allowed v1.1 filters (`assignee`, `priority`, `type`).
- ADR defines change policy as additive/non-breaking for v1.x, with explicit out-of-scope items.
- Follow-on implementation tickets are split into <=1 day tasks and linked from ADR.

**Architecture notes**
- Maintain task-first bounded context; keep project APIs deferred/non-routable.
- Preserve canonical lifecycle semantics (`CANCELED` stays authoritative terminal state unless superseded by ADR).

---

### ATS-PA-25 — MCP contract pack: tool schemas, error mapping, and consumer examples
**Problem**: MCP runtime contracts are tested, but consumer-facing contract readability/reuse is weak.

**Scope slices (<=1 day each)**
- 25a: Add `docs/mcp-contract-pack.md` with tool inputs/outputs and error-code mapping table.
- 25b: Add CI-validated JSON examples for success + canonical failures (validation/conflict/not found/idempotency mismatch).
- 25c: Add drift check ensuring docs examples remain aligned with runtime tool schemas.

**Acceptance criteria**
- Each shipped MCP task tool has at least 1 valid request/response example and 1 failure example.
- Error code mapping is explicitly parity-aligned with REST problem `code` vocabulary.
- CI fails if schema/example mismatch is introduced.

**Architecture notes**
- Keep one canonical error-code vocabulary shared across adapters.
- Treat examples as contract artifacts, not informal docs.

---

### ATS-PA-26 — Micronaut architecture guardrails (module boundaries + profile safety)
**Problem**: package boundaries and environment profile expectations rely on convention more than enforcement.

**Scope slices (<=1 day each)**
- 26a: Add architecture tests asserting adapter->application->domain dependency direction.
- 26b: Add profile safety tests for critical keys (`task.store`, management endpoint posture, Mongo URI expectation).
- 26c: Document allowed dependency rules in `ARCHITECTURE.md` and config matrix cross-reference.

**Acceptance criteria**
- Automated tests fail on forbidden dependencies (e.g., adapter directly depending on infrastructure internals).
- Profile tests assert expected defaults for `local/test/prod` and fail on accidental drift.
- Architecture doc includes explicit dependency-direction policy and profile invariants.

**Architecture notes**
- Domain and application contracts remain transport/storage-agnostic.
- Profiles are release-safety controls, not just convenience defaults.

---

### ATS-PA-27 — Mongo migration + reliability contract (index/schema evolution runbook)
**Problem**: startup index creation is good baseline, but destructive changes and recovery playbooks are not codified.

**Scope slices (<=1 day each)**
- 27a: Add migration runbook doc for destructive index/schema changes (drop/rename/uniqueness changes).
- 27b: Add reliability test pack for duplicate-key races + transient write/retry behavior.
- 27c: Add release-evidence checklist items for migration risk sign-off.

**Acceptance criteria**
- Runbook defines pre-checks, backup/rollback, and post-verify steps for destructive changes.
- Tests cover duplicate-key contention and transient failure retry contract deterministically.
- Release evidence includes explicit migration-risk section when Mongo contract changes ship.

**Architecture notes**
- Keep startup behavior non-destructive; destructive operations require planned/manual migration path.
- Preserve deterministic index names/specs for operational auditability.

---

### ATS-PA-28 — Cross-transport RED + parity-drift observability baseline
**Problem**: current telemetry is strong for idempotency but not yet complete for transport-level reliability and parity health.

**Scope slices (<=1 day each)**
- 28a: Introduce shared telemetry seam for request rate/error/duration by `{transport,operation,outcome}`.
- 28b: Instrument REST endpoints + MCP tool handlers using shared seam.
- 28c: Add parity-drift counter for mapping mismatches and extend `OBSERVABILITY.md` with starter alerts.

**Acceptance criteria**
- Metrics emitted for REST and MCP with bounded-cardinality tags only.
- At least one automated test asserts meter names + required tags.
- Observability docs define metric semantics and suggested initial alert thresholds.

**Architecture notes**
- Centralize instrumentation in shared application seam; avoid duplicate adapter-specific logic.
- Reuse canonical outcome/error vocabulary to keep dashboards comparable.

---

## P1 (next)

### ATS-PA-29 — Unified error-catalog source generation
- Generate REST problem mapping + MCP mapping + OpenAPI error components from one source.
- AC: CI fails on divergence between generated artifacts and runtime mappings.

### ATS-PA-30 — List-query v1.1 delivery (after ATS-PA-24)
- Implement only ADR-approved filter/sort scope and update parity/openapi tests together.
- AC: OpenAPI + MCP schemas + parity tests stay synchronized.

### ATS-PA-31 — Local DX verification command (`verifyLocal`)
- Add one-command profile-aware local verification wrapper and doc quickstart.
- AC: fresh clone can run documented command with deterministic pass/fail semantics.

### ATS-PA-32 — Correlation-id normalization unification component
- Move any remaining adapter-inline normalization to shared contract component.
- AC: no transport drift in malformed/blank correlation-id behavior.

---

## Recommended execution order
1. ATS-PA-24 (scope lock first)
2. ATS-PA-26 (architecture/profile guardrails)
3. ATS-PA-25 (MCP contract pack)
4. ATS-PA-27 (Mongo migration/reliability)
5. ATS-PA-28 (observability baseline)
6. ATS-PA-29
7. ATS-PA-30
8. ATS-PA-31
9. ATS-PA-32

---

## Definition of Done (all ATS-PA tickets)
- `./gradlew check` passes (or CI fallback explicitly documented with rationale + source label).
- No REST/MCP contract drift introduced without explicit ADR-approved change.
- Docs updated in same change set for any contract/profile/schema/observability behavior change.
- Release-significant changes include evidence-source declaration (`local-java21` or `ci-java21`) and rollback note.

## Next top 3 developer tickets
1. **ATS-PA-24** — Lock API completeness boundary via ADR before new surface area.
2. **ATS-PA-26** — Add architecture + profile guardrail tests to prevent silent drift.
3. **ATS-PA-25** — Publish MCP contract pack with CI-validated examples and parity mapping.
