# ADR-011: MCP Correlation ID Source Policy (Interim)

- **Status:** Accepted
- **Date:** 2026-02-24
- **Deciders:** product/architecture
- **Related:** `ADR-010-cross-transport-correlation-id-contract.md`, `ARCHITECTURE.md`, `PRODUCT_OWNER_NEXT.md`

## Context
ADR-010 locked the requirement that MCP failures expose `correlationId`, but it did not define how MCP should treat caller-supplied IDs.

Current code path (`TaskMcpTools`) always generates a UUID per invocation for failures. The tool request DTOs do not carry a standard `correlationId` field, and MCP transport metadata conventions for correlation propagation are not yet standardized in this repo.

## Decision
1. **Interim v1 policy:** MCP failures MUST include non-empty `correlationId`; server-generated fallback is the default/required behavior.
2. **No implicit caller-token reuse yet:** until a stable transport metadata contract is chosen, MCP will not claim caller-provided correlation propagation parity with REST.
3. **Future adoption gate:** caller-token propagation can be added only with:
   - explicit source precedence rules (tool argument vs transport metadata),
   - regression tests for propagation + fallback,
   - documentation updates in architecture/handoff/product backlog.

## Why
- Prevents ambiguous behavior claims while still satisfying cross-transport debuggability.
- Avoids coupling to unstable or implicit MCP transport metadata assumptions.
- Keeps a clear upgrade path once propagation semantics are designed.

## Consequences
### Positive
- Contract language now matches current implementation reality.
- Reduces accidental drift between docs and runtime behavior.

### Tradeoffs
- REST remains ahead of MCP on caller-token echo semantics.
- A follow-up ticket is required to reach full parity.

## Guardrails
- `correlationId` presence on MCP failures remains mandatory.
- Any shift to caller propagation requires a superseding ADR and parity test updates.
