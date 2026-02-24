# ADR-004: Finalize Concurrency and Idempotency-Mismatch API Contract

- **Status:** Accepted (implementation in progress)
- **Date:** 2026-02-23
- **Last updated:** 2026-02-24
- **Deciders:** product/architecture, backend maintainers
- **Related:** `ADR-003-idempotency-and-concurrency-contract.md`, `ARCHITECTURE.md`

## Context
Current implementation supports optimistic locking and idempotency replay but left two externally visible behaviors under-specified:
1. Which stable error code should represent optimistic-lock write conflicts.
2. What should happen when the same idempotency key is reused with a different payload.

This ambiguity risks adapter drift and client retry bugs, especially once MCP tools are added.

## Decision
1. **Concurrency conflicts use:** `CONCURRENT_MODIFICATION` (HTTP 409).
2. **Idempotency mismatch policy:** reject key reuse with different payload for same operation using HTTP 409 and code `IDEMPOTENCY_KEY_REUSE_MISMATCH`.
3. **Idempotency uniqueness scope:** `(operation, key)`.
4. **Idempotency records persist:** `operation`, `key`, `payloadHash`, `resultRef`, `expiresAt`, `createdAt`.

## Why
- Gives clients deterministic, contract-safe retry semantics.
- Prevents silent data corruption from accidental key reuse.
- Keeps REST and future MCP behavior aligned by policy.

## Consequences
### Positive
- Clear operational signals for retries vs conflicts.
- Easier parity tests and contract governance.

### Tradeoffs
- Requires payload canonicalization/hashing strategy.
- Adds storage and validation complexity to mutation path.

## Implementation status (as of 2026-02-24)
- ✅ `CONCURRENT_MODIFICATION` is emitted by REST error mapping and covered by test.
- ❌ `IDEMPOTENCY_KEY_REUSE_MISMATCH` is not yet implemented.
- ❌ Mongo idempotency uniqueness is still key-only; operation scope not yet applied.
- ❌ TTL uses `createdAt` with relative expiry; target contract is explicit `expiresAt` TTL index.

## Implementation notes
- Update `ApiExceptionHandler`/tests for mismatch mapping.
- Evolve idempotency document/index model to include operation and payload hash.
- Add explicit replay/mismatch integration tests.
- Update error catalog documentation and OpenAPI examples.
