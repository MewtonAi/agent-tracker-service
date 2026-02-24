# ADR-004: Finalize Concurrency and Idempotency-Mismatch API Contract

- **Status:** Accepted
- **Date:** 2026-02-23
- **Last updated:** 2026-02-24
- **Deciders:** product/architecture, backend maintainers
- **Related:** `ADR-003-idempotency-and-concurrency-contract.md`, `ADR-005-idempotency-v2-rollout-strategy.md`, `ARCHITECTURE.md`

## Context
Two externally visible behaviors required hard contract decisions:
1. Which stable error code represents optimistic-lock write conflicts.
2. What happens when the same idempotency key is reused with a different payload.

These decisions are foundational for safe retries and for REST/MCP parity.

## Decision
1. **Concurrency conflicts use:** `CONCURRENT_MODIFICATION` (HTTP 409).
2. **Idempotency mismatch policy:** reject same `(operation,key)` with different payload using HTTP 409 and code `IDEMPOTENCY_KEY_REUSE_MISMATCH`.
3. **Idempotency uniqueness scope:** `(operation, key)`.
4. **Idempotency records persist:** `operation`, `key`, `payloadHash`, `resultRef`, `expiresAt`, `createdAt`, `updatedAt`.

## Why
- Deterministic client retry semantics.
- Explicit rejection of unsafe key reuse.
- Shared policy surface for REST and future MCP adapters.

## Consequences
### Positive
- Stable conflict taxonomy for clients and telemetry.
- Reduced adapter drift risk once MCP tools are added.

### Tradeoffs
- Requires payload-hash consistency and contract tests.
- Adds persistence/index complexity for mutation paths.

## Implementation status (verified 2026-02-24)
- ✅ `CONCURRENT_MODIFICATION` mapped in API handler + tests.
- ✅ `IDEMPOTENCY_KEY_REUSE_MISMATCH` mapped in API handler + tests.
- ✅ Mongo idempotency uniqueness uses `(operation,key)`.
- ✅ TTL index uses explicit `expiresAt`.

## Guardrails
- Any change to these codes requires ADR + error-catalog/openapi updates.
- MCP transport must reuse the same application/store behavior; no MCP-only conflict semantics.
