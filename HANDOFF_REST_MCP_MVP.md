# Developer Handoff — REST + MCP Post-MVP Hardening

Last updated: 2026-02-24 (PST, late-night refresh)

## What changed in this product/architecture pass
- Performed fresh repo inspection across code, docs, tests, and CI wiring.
- Re-prioritized roadmap around the highest-impact post-MVP gap: **cross-transport correlation ID parity**.
- Added decision record:
  - `ADR-010-cross-transport-correlation-id-contract.md`
  - Locks requirement that both REST and MCP failures expose correlation IDs.
- Refreshed planning docs (`PRODUCT_OWNER_NEXT.md`, `ARCHITECTURE.md`) to reflect new execution order and acceptance criteria.

## Current engineering baseline (verified)
- REST task endpoints stable for create/get/list/update-status.
- MCP tools implemented with shared application services.
- CI gate executes `./gradlew check` on push/PR (JDK 21).
- Contract suites present and gating:
  - REST/MCP parity
  - MCP registration + runtime transport handshake/discoverability
  - OpenAPI strict snapshot drift
  - Error-code catalog lock
- Idempotency counters and alert-threshold guidance are documented and test-covered.

## Recommended next coding slice
### TKT-P1-O12 — Cross-transport correlation ID propagation contract
Definition of done:
- MCP tool error payload includes `correlationId` (non-empty).
- REST correlation behavior remains unchanged and guarded.
- Contract/parity tests cover caller-provided + generated fallback IDs across representative errors:
  - `TASK_NOT_FOUND`
  - `CONCURRENT_MODIFICATION`
  - `IDEMPOTENCY_KEY_REUSE_MISMATCH`
- Docs updated where external error contract is described.

## Suggested implementation notes
- Keep stable error `code` values unchanged while extending MCP error shape.
- Prefer one correlation token generation strategy reusable across adapters.
- Add tests first (or alongside implementation) to avoid contract drift.

## Next slices after O12
1. `TKT-P1-A13` cursor pagination parity contract.
2. `TKT-P1-A14` internalize deferred project DTO/contracts.

## Validation note
- This pass focused on product/architecture artifacts; no runtime code changes were made.
- Local execution of `./gradlew check` was not rerun in this docs-only pass.
