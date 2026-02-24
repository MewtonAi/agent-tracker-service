# ADR-006: REST/MCP Parity and Contract Governance as MVP Release Gate

- **Status:** Accepted
- **Date:** 2026-02-24
- **Deciders:** product/architecture
- **Related:** `ADR-002-v1-scope-and-parity.md`, `ADR-004-idempotency-mismatch-and-conflict-code.md`, `ARCHITECTURE.md`

## Context
REST v1 behavior is implemented, while MCP adapter work is pending. Without explicit parity and contract-governance gates, the project risks:
- semantic drift between transports
- accidental breaking contract changes
- reduced confidence in retry/conflict behavior for clients

## Decision
1. **MVP release requires REST/MCP parity suite for canonical task flows.**
2. **MVP release requires checked-in OpenAPI snapshot and CI drift gate.**
3. **Externally visible error `code` values are treated as contract surface and locked by tests.**

## Why
- Ensures one business behavior regardless of transport.
- Makes contract changes intentional and reviewable.
- Protects client integrations from silent breakage.

## Consequences
### Positive
- Faster detection of semantic regressions.
- Better release confidence and reproducibility.

### Tradeoffs
- Additional CI/runtime cost.
- Requires ongoing discipline to update snapshots intentionally.

## Guardrails
- Parity tests execute the same intent scenarios against both adapters.
- OpenAPI snapshot updates must be explicit in PR scope.
- Error code changes require ADR and changelog note.
