# Developer Handoff — REST + MCP MVP / Post-MVP Plan

Last updated: 2026-02-24 (PST, late)

## What changed in this product/architecture pass
- Refreshed architecture doc to reflect **actual current state** (MCP runtime handshake + strict OpenAPI drift gate are complete, not pending).
- Refined product roadmap/backlog with prioritized post-MVP epics and implementation-ready tickets:
  - `TKT-P1-O11` idempotency metrics/alerts hardening (next)
  - `TKT-P1-A13` cursor pagination parity
  - `TKT-P1-A14` API package hygiene for deferred project artifacts
- Added new decision record:
  - `ADR-009-idempotency-observability-sli-contract.md`
  - Locks required idempotency counters + SLI formulas (replay ratio, mismatch rate)
- Updated `README.md` to remove stale “next priorities” that were already completed.

## Current engineering baseline (code reality)
- REST task endpoints are implemented and stable for core flows.
- MCP adapter (`TaskMcpTools`) is implemented for create/get/list/update-status.
- REST/MCP parity tests are CI-gated (`TaskRestMcpParityTest`).
- MCP runtime transport gate verifies HTTP JSON-RPC handshake + `tools/list` discoverability.
- OpenAPI contract gate validates generated-vs-checked-in equality (`verifyOpenApiSnapshot`).
- Error contract includes stable 409 mappings for:
  - `CONCURRENT_MODIFICATION`
  - `IDEMPOTENCY_KEY_REUSE_MISMATCH`
- Mongo idempotency v2 behavior is present and documented.

## Recommended next implementation slice
### TKT-P1-O11 — Idempotency metrics/alerts hardening
Definition of done:
- Durable counters emitted for first-write/replay/mismatch by operation.
- Replay ratio + mismatch rate surfaced in a repo-documented dashboard spec.
- Alert thresholds + first-response runbook notes documented.
- Coverage tests prove counter emission paths in create + status update flows.

## Validation note
- Attempted local `./gradlew check` during this pass, but environment lacked Java setup (`JAVA_HOME`/`java` not found).
- CI remains configured to run `./gradlew check` on push/PR (`.github/workflows/ci.yml`).

## Watch-outs for implementer
- Preserve ADR-007 posture (idempotency v2-only semantics) unless superseded by ADR.
- If Micronaut MCP transport conventions change in dependency upgrades, update runtime transport contract test before release.
