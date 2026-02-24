# ADR-007: Idempotency Migration Posture is v2-Only (No Legacy Fallback)

- **Status:** Accepted
- **Date:** 2026-02-24
- **Deciders:** product/architecture
- **Related:** `ADR-003-idempotency-and-concurrency-contract.md`, `ADR-004-idempotency-mismatch-and-conflict-code.md`, `ADR-005-idempotency-v2-rollout-strategy.md`, `ARCHITECTURE.md`

## Context
The current codebase already persists and reads idempotency records using explicit v2 fields (`operation`, `key`, `payloadHash`, `resultRef`, `expiresAt`) and v2 indexes (`{operation:1,key:1}` unique + TTL on `expiresAt`).

No legacy key-only replay read path exists in the repository at this time.

ADR-005 documented a conservative two-phase migration strategy, but that strategy does not match implemented behavior in this repo lineage.

## Decision
1. **Canonical posture is v2-only idempotency semantics.**
2. **Legacy key-only fallback replay is explicitly out of scope and must not be introduced without a new ADR.**
3. **Documentation and tests should treat fallback absence as intentional, not a temporary gap.**

## Why
- Keeps behavior explicit and aligned with real implementation.
- Avoids carrying dead migration complexity where no legacy records are expected.
- Reduces ambiguity for MCP/REST parity and contract tests.

## Consequences
### Positive
- Simpler mental model and lower implementation surface area.
- Clearer release messaging for clients/operators.

### Tradeoffs
- If legacy records ever appear in a future environment, retries using legacy shape would not replay.
- Any future compatibility need will require explicit design and guarded rollout.

## Guardrails
- Preserve mismatch behavior for same `(operation,key)` with different `payloadHash`: `IDEMPOTENCY_KEY_REUSE_MISMATCH` (HTTP 409).
- Keep idempotency retention configurable via `idempotency.ttl-hours`.
- Add/maintain tests that lock v2 semantics and reject accidental fallback reintroduction assumptions.
