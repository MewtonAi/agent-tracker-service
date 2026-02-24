# PRODUCT_OWNER_NEXT.md

Last updated: 2026-02-24 (PST, backlog re-prioritized against current code state)
Owner: Product/Architecture

## Current run outcome
- Re-inspected repo implementation and docs for REST + MCP readiness with Micronaut + Mongo.
- Refreshed `docs/product-architecture-backlog.md` to reflect what is already implemented vs true remaining gaps.
- Re-prioritized toward highest leverage closure: API boundary ADR, Mongo index auditability, cross-transport observability.
- Added tighter execution slices (<=1 day), concrete acceptance criteria, dependencies, and architecture notes.

## Priority stack (now)
### P0
1. ATS-PA-02 — ADR: API completeness boundary (archive/delete + assignment + list filter scope)
2. ATS-PA-04 — Mongo index/migration manifest + startup verification contract
3. ATS-PA-05 — Cross-transport observability baseline (REST + MCP RED + parity mismatch)
4. ATS-PA-11 — Shared correlation-id normalization component

### P1
5. ATS-PA-07 — Unified error catalog single-source generator/check
6. ATS-PA-08 — Mongo resilience testing pack
7. ATS-PA-06 — List query contract v1.1 implementation (post ADR in ATS-PA-02)
8. ATS-PA-09 — MCP consumer examples pack
9. ATS-PA-10 — DX local verification guardrails

## Canonical backlog artifact
- `docs/product-architecture-backlog.md`

## Next top 3 developer tickets
1. ATS-PA-02
2. ATS-PA-04
3. ATS-PA-05
