# PRODUCT_OWNER_NEXT.md

Last updated: 2026-02-24 (PST, continuous product/architecture pass)
Owner: Product/Architecture

## Current run outcome
- Re-inspected repository state (code + docs + backlog artifacts) for REST + MCP readiness on Micronaut + Mongo.
- Reworked `docs/product-architecture-backlog.md` into a sharper, execution-ready queue with explicit coverage for:
  - REST API completeness/consistency
  - MCP contract/tool readiness
  - Micronaut architecture boundaries + profile controls
  - Mongo migration/reliability posture
  - test strategy, observability, and developer UX
- Added concrete acceptance criteria and architecture notes for top-priority tickets.

## Priority stack

### P0
1. **ATS-PA-24** — ADR: v1.1 API completeness boundary (archive/delete, assignment, list-filter scope)
2. **ATS-PA-26** — Micronaut architecture guardrails (dependency direction + profile safety tests)
3. **ATS-PA-25** — MCP contract pack (schemas + error mapping + CI-validated examples)
4. **ATS-PA-27** — Mongo migration + reliability contract (runbook + resilience tests)
5. **ATS-PA-28** — Cross-transport RED + parity-drift observability baseline

### P1
6. ATS-PA-29 — Unified error-catalog source generation
7. ATS-PA-30 — List-query v1.1 implementation (post ATS-PA-24)
8. ATS-PA-31 — Local DX verification command (`verifyLocal`)
9. ATS-PA-32 — Correlation-id normalization unification

## Canonical backlog artifact
- `docs/product-architecture-backlog.md`

## Next top 3 developer tickets
1. ATS-PA-24
2. ATS-PA-26
3. ATS-PA-25
