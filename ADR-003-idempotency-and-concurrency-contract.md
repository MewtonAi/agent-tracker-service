# ADR-003: Durable Idempotency and Optimistic Concurrency Contract

- **Status:** Accepted
- **Date:** 2026-02-23
- **Deciders:** product/architecture
- **Related:** `ADR-001.md`, `ADR-002-v1-scope-and-parity.md`, `ARCHITECTURE.md`

## Context
Current mutation behavior uses in-memory idempotency caches. This is useful for local correctness but insufficient for production reliability:
- retries across process restart can duplicate effects
- horizontally scaled replicas cannot share replay history
- concurrent updates require deterministic conflict handling

## Decision
1. **Mutations are idempotency-keyed and durably recorded.**
   - Applies to `POST /v1/tasks` and `PATCH /v1/tasks/{id}/status` (and equivalent MCP tools).
2. **Idempotency scope is `(operation, idempotencyKey)`**.
   - Same key reused for different operation is invalid.
3. **Task writes use optimistic locking (`version`)** with compare-and-set updates.
4. **Conflict semantics are explicit and stable.**
   - Transition-rule conflicts: `INVALID_TASK_TRANSITION` (409)
   - Concurrency conflicts: `CONCURRENT_MODIFICATION` (409)
5. **Replay returns original logical result** and is observable in logs/metrics.

## Why
- Enables safe client retries in distributed systems.
- Keeps consistency guarantees without pessimistic locks.
- Aligns REST and MCP mutation behavior around shared guarantees.

## Alternatives considered

### 1) Process-local idempotency only
- **Pros:** simple
- **Cons:** unsafe across restarts/replicas
- **Result:** rejected

### 2) Pessimistic locking
- **Pros:** simpler conflict reasoning
- **Cons:** throughput/latency penalty, lock-management complexity
- **Result:** rejected for MVP

### 3) Global key scope without operation dimension
- **Pros:** fewer dimensions
- **Cons:** accidental collisions across endpoints/tools
- **Result:** rejected

## Consequences
### Positive
- Deterministic, testable retry semantics.
- Scales safely across instances.
- Cleaner observability around replay/conflict behavior.

### Tradeoffs
- Additional persistence/storage overhead for idempotency records.
- Requires retention policy and cleanup strategy.

## Guardrails
- Idempotency record must include operation, key, request fingerprint/hash, and stored result reference.
- TTL retention configurable (default recommended 48h).
- Reuse of same `(operation,key)` with mismatched payload should fail with 409/400 policy (implementation must pick and document one behavior consistently).

## Implementation notes
- Collections:
  - `tasks` with `version`
  - `idempotency_records` with unique index on `(operation, key)` and TTL index on `expiresAt`
- Ensure create/status-update flows write idempotency record atomically with mutation outcome where feasible.
- Add parity tests validating identical replay/conflict behavior in REST and MCP.
