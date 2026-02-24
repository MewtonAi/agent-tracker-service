# PRODUCT_OWNER_NEXT.md

Last updated: 2026-02-24 (PST, product/architecture backlog refinement run)
Owner: Product/Architecture

## Current run outcome
- Inspected repo code/docs/backlog state for REST + MCP readiness on Micronaut + Mongo.
- Produced a refreshed prioritized backlog focused on:
  - REST API completeness/consistency
  - MCP contract/tool readiness
  - Micronaut modular/config-profile posture
  - Mongo schema/index/migration reliability
  - test strategy, observability, and DX
- Added: `docs/product-architecture-backlog.md` with concrete ticket list, acceptance criteria, and architecture notes.

## Priority stack (now)
### P0
1. ATS-PA-01 — shared REST/MCP contract normalization component
2. ATS-PA-02 — ADR for API completeness (task end-of-life + assignment semantics)
3. ATS-PA-03 — Micronaut profile baseline (`local`/`test`/`prod`) + config matrix
4. ATS-PA-04 — Mongo migration/index manifest + startup verification log contract
5. ATS-PA-05 — Cross-transport observability baseline (REST + MCP RED + parity signal)

### P1
6. ATS-PA-06 — list query contract v1.1 (sort/filter expansion)
7. ATS-PA-07 — unified error catalog alignment gate (OpenAPI + MCP)
8. ATS-PA-08 — Mongo resilience testing pack
9. ATS-PA-09 — MCP tool contract example pack
10. ATS-PA-10 — DX guardrails for local verification

## Next top 3 developer tickets
1. ATS-PA-01
2. ATS-PA-03
3. ATS-PA-04

## Canonical backlog artifact
- `docs/product-architecture-backlog.md`
