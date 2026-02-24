# Cursor Evolution Phase-2 Plan (ADR-015)

Last updated: 2026-02-24 (PST)
Owner: Engineering (REST + MCP contract governance)

## Goal
Define a seek-token migration path that improves pagination scalability while preserving the external v1 contract fields (`cursor`, `nextCursor`, `limit`) and REST/MCP parity.

## Non-goals
- No external envelope field changes in phase-2.
- No removal of legacy offset cursor decoding in phase-2.
- No transport-specific cursor semantics (REST and MCP stay parity-bound).

## Contract constraints (must remain true)
1. Request field remains `cursor`; response field remains `nextCursor`.
2. `limit` bounds and validation behavior remain unchanged.
3. Legacy offset tokens (`<n>`) remain accepted.
4. Prefixed offset tokens (`o:<n>`) remain accepted.
5. Unknown token families (for example `s:<payload>`) remain rejected until explicitly introduced.

## Migration phases

### Phase 2A — Internal readiness (no emitted token change)
- Keep emission as legacy offset token (`nextCursor=<n>`).
- Add decoder abstraction seams so token-family parsing and validation are centralized.
- Add explicit telemetry markers for token family usage (`offset-legacy`, `offset-prefixed`, `unknown-family`).

### Phase 2B — Dual-emission canary (controlled rollout)
- Introduce optional emission flag for seek-family token format (for example `s:<opaque>`), disabled by default.
- Decoder supports both offset + seek tokens under feature flag.
- Rollout in canary environments only; parity tests must run in both emission modes.

### Phase 2C — Default seek emission with compatibility fallback
- Make seek token emission default when rollback guard is available.
- Continue accepting offset tokens for backward compatibility.
- Retain rollback switch to revert emission to offset without API/schema changes.

## Rollback posture
- Runtime-configurable flag toggles emitted token family.
- Decoder always remains superset-compatible (offset + enabled seek) during rollout.
- If seek mode incidents occur, rollback is emission-only; no wire-contract revert required.

## Mixed-token REST/MCP parity test plan

### Required parity scenarios
1. **Legacy caller flow**: request with `<n>` cursor in REST and MCP produces equivalent pages.
2. **Prefixed-offset caller flow**: request with `o:<n>` cursor in REST and MCP produces equivalent pages.
3. **Mixed progression flow**: client starts with offset token, receives seek token (when enabled), and continues paging successfully across both transports.
4. **Terminal-page semantics**: when no additional records exist, both transports return `nextCursor = null`.
5. **Malformed token handling**: non-numeric/negative/unknown-family tokens map to bad-request code parity across REST + MCP.
6. **Whitespace/case normalization**: `O: <n>` style input remains accepted and parity-aligned.

### Verification matrix
- Run parity suite with emission mode `offset` and `seek`.
- Validate cursor compatibility contract for both in-memory and Mongo stores.
- Confirm OpenAPI snapshot stays stable (field names unchanged).

## Release checklist additions (phase-2)
- Include emission mode tested in release evidence.
- Include mixed-token parity results summary.
- Include explicit rollback flag value and rollback owner.
