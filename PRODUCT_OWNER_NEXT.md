# PRODUCT_OWNER_NEXT.md

Last updated: 2026-02-24 (PST, execution-ready backlog refinement)
Owner: Product/Architecture

## Current run outcome
- Re-inspected repo posture for REST + MCP readiness with Micronaut + Mongo.
- Refined `docs/product-architecture-backlog.md` into execution-ready ticket specs.
- Added tighter acceptance criteria, dependency hints, architecture notes, and a shared Definition of Done.
- Kept focus on: REST completeness/consistency, MCP tool readiness, profile architecture, Mongo reliability, observability, and DX.

## Priority stack (now)
### P0
1. ATS-PA-01 — shared REST/MCP contract normalization component
2. ATS-PA-03 — Micronaut profile baseline (`local`/`test`/`prod`) + config matrix
3. ATS-PA-04 — Mongo migration/index manifest + startup verification contract
4. ATS-PA-05 — cross-transport observability baseline (REST + MCP RED + parity)
5. ATS-PA-02 — ADR to lock API completeness semantics (archive/delete + assignment)

### P1
6. ATS-PA-06 — list query contract v1.1 (sort/filter expansion)
7. ATS-PA-07 — unified error catalog alignment gate (OpenAPI + MCP)
8. ATS-PA-08 — Mongo resilience testing pack
9. ATS-PA-09 — MCP tool contract examples pack
10. ATS-PA-10 — DX local verification guardrails

## Canonical backlog artifact
- `docs/product-architecture-backlog.md`

## Next top 3 developer tickets
1. ATS-PA-01
2. ATS-PA-03
3. ATS-PA-04
