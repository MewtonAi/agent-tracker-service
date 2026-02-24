# ADR-008: MVP Gate Tightening for MCP Transport Handshake and OpenAPI Drift

- **Status:** Accepted
- **Date:** 2026-02-24
- **Deciders:** product/architecture
- **Related:** `ADR-006-rest-mcp-parity-and-contract-governance-gate.md`, `ARCHITECTURE.md`, `PRODUCT_OWNER_NEXT.md`

## Context
MVP governance work is partially complete:
- REST/MCP scenario parity is CI-gated.
- OpenAPI snapshot is committed and marker-validated.
- MCP runtime test validates boot plus tool bean/method signatures.

Two residual gaps can still allow silent contract drift:
1. MCP runtime checks do not yet prove transport-wire discoverability (`tools/list`).
2. OpenAPI verification checks markers only, not full generated-vs-snapshot equality.

## Decision
1. **MVP release gate requires transport-wire MCP handshake verification (`tools/list`).**
2. **MVP release gate requires deterministic OpenAPI generated-vs-checked-in diff enforcement.**
3. Marker-only OpenAPI checks and reflection-only MCP checks are treated as interim safeguards, not sufficient release gates.

## Why
- Prevents false confidence from context-level/runtime reflection tests.
- Makes API contract drift explicit and reviewable.
- Aligns MVP quality bar with external integrator expectations.

## Consequences
### Positive
- Stronger release confidence for both REST and MCP clients.
- Faster diagnosis when adapter registration or schema exposure regresses.

### Tradeoffs
- Slightly longer CI runs and additional test harness complexity.
- Requires deterministic OpenAPI generation workflow discipline.

## Guardrails
- MCP gate must assert discoverability of all task tools and required inputs.
- OpenAPI gate must fail on any structural drift unless snapshot update is explicitly included.
- Any relaxation of these gates requires a follow-up ADR.
