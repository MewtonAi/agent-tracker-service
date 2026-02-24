# ADR-005: Idempotency v2 Rollout Strategy (Mongo)

- **Status:** Superseded by ADR-007
- **Date:** 2026-02-24
- **Deciders:** product/architecture
- **Related:** `ADR-003-idempotency-and-concurrency-contract.md`, `ADR-004-idempotency-mismatch-and-conflict-code.md`, `ADR-007-idempotency-v2-only-posture.md`

## Context
The codebase currently stores idempotency as a single string key (including encoded operation/task scope), with unique index on `{ key: 1 }` and TTL on `createdAt`.

Target contract (ADR-003/004) requires durable semantics on explicit fields:
- uniqueness on `(operation, key)`
- mismatch rejection based on request payload fingerprint
- explicit expiry field (`expiresAt`) with TTL index

A direct one-shot replacement risks replay regressions for in-flight retries and existing records.

## Decision
Adopt a **two-phase migration strategy**:

1. **Phase A (compatibility): dual-read, v2-write**
   - Writes persist v2 shape (`operation`, `key`, `payloadHash`, `resultRef`, `expiresAt`).
   - Replay reads check v2 first.
   - For a bounded grace window, replay reads may fallback to legacy key-only records.

2. **Phase B (cleanup): v2-only**
   - Remove fallback read path after the grace window.
   - Remove legacy unique index and legacy document assumptions.

3. **Mismatch policy**
   - Same `(operation,key)` with different `payloadHash` returns 409 `IDEMPOTENCY_KEY_REUSE_MISMATCH`.

## Why
- Prevents breaking retries during rollout.
- Enables safe incremental delivery and rollback.
- Keeps semantics explicit and testable across REST and MCP.

## Consequences
### Positive
- Lower migration risk in production-like environments.
- Clear path to contract-compliant idempotency semantics.

### Tradeoffs
- Temporary complexity (dual-read behavior).
- Requires explicit decommission checkpoint for legacy behavior.

## Guardrails
- Grace window must be configurable and time-bounded.
- Add telemetry counters for legacy fallback reads.
- MCP adapters must use the same idempotency service path (no adapter-specific handling).

## Follow-up tickets
- Introduce v2 idempotency schema + indexes.
- Implement mismatch detection and error mapping.
- Add migration/fallback integration tests.
- Remove legacy fallback path once telemetry confirms negligible use.
