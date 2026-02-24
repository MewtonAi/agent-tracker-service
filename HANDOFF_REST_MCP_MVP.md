# Developer Handoff — REST + MCP MVP

Last updated: 2026-02-24 (PST)

## What was done in this developer pass
- Implemented MCP wire-level transport contract verification in runtime test coverage:
  - `TaskMcpRuntimeTransportContractTest` now performs HTTP `initialize` then `tools/list` JSON-RPC calls.
  - Test asserts all 4 task tools are discoverable (`createTask`, `getTask`, `listTasks`, `updateTaskStatus`).
  - Test asserts schema-critical field markers appear in transport response (`idempotencyKey`, `taskId`).
- Hardened OpenAPI drift governance from marker-only checks to strict equality gate:
  - `verifyOpenApiSnapshot` now depends on generated OpenAPI output and fails on any snapshot drift.
  - Existing required marker assertions are retained for route/error-code guarantees.
- Updated `README.md` and planning docs to reflect MVP gate completion and post-MVP focus.

## Current engineering baseline (code reality)
- REST task endpoints are implemented and stable for core flows.
- MCP application adapter (`TaskMcpTools`) is implemented for create/get/list/update-status.
- REST/MCP parity tests are CI-gated (`TaskRestMcpParityTest`).
- MCP runtime transport smoke now validates wire handshake + discovery (`TaskMcpRuntimeTransportContractTest`).
- OpenAPI contract gate now validates generated-vs-checked-in equality (`verifyOpenApiSnapshot`).
- Error contract includes stable 409 mappings for:
  - `CONCURRENT_MODIFICATION`
  - `IDEMPOTENCY_KEY_REUSE_MISMATCH`
- Mongo idempotency v2 behavior is present:
  - uniqueness on `(operation,key)`
  - mismatch detection by payload hash
  - TTL index on `expiresAt` with configurable retention
- Migration posture is explicitly v2-only (ADR-007).
- Idempotency observability markers are emitted:
  - `idempotency.first_write`
  - `idempotency.replay_hit`
  - `idempotency.mismatch_reject`

## Remaining work (post-MVP)
### TKT-P1-O11 — Idempotency metrics/alerts hardening
Definition of done:
- Counters exported for first-write/replay/mismatch by operation.
- Dashboard for replay ratio and mismatch rate.
- Alert thresholds documented.

## Local validation commands
```bash
./gradlew check
./gradlew test --tests "*TaskMcpRuntimeTransportContractTest"
./gradlew verifyOpenApiSnapshot
```

## Notes / risks for next implementer
- MCP transport endpoint path is resolved dynamically in test via router inspection to reduce hardcoding fragility.
- If Micronaut MCP transport protocol/header names change in dependency upgrades, update the runtime transport test payload/header assumptions accordingly.
- No functional domain behavior changed in this pass; focus remained on contract/runtime gate hardening.
