# ADR-010: Cross-Transport Correlation ID Contract (REST + MCP)

- **Status:** Accepted
- **Date:** 2026-02-24
- **Deciders:** product/architecture
- **Related:** `ARCHITECTURE.md`, `PRODUCT_OWNER_NEXT.md`, `HANDOFF_REST_MCP_MVP.md`

## Context
REST error responses already expose `correlationId` and echo `X-Correlation-Id`. MCP tools currently return stable error `code` values but do not expose an explicit correlation identifier contract for failures.

As REST + MCP usage grows, support and incident workflows need a single trace token strategy that works across adapters, especially for conflict/idempotency failures where parity is expected.

## Decision
1. Define a transport-agnostic requirement: every externally surfaced failure must carry a correlation identifier.
2. REST contract remains unchanged:
   - accept caller `X-Correlation-Id` when provided
   - generate one when missing
   - return it in response header + error payload `correlationId`
3. MCP contract adds `correlationId` to tool error payloads (alongside stable `code` and message).
4. Correlation ID generation/propagation behavior is contract-governed via automated tests covering:
   - caller-supplied value propagation
   - server-generated fallback when missing
   - parity across representative REST + MCP error scenarios (`TASK_NOT_FOUND`, `CONCURRENT_MODIFICATION`, `IDEMPOTENCY_KEY_REUSE_MISMATCH`).

## Why
- Reduces MTTR by giving operators one trace handle regardless of adapter.
- Makes support playbooks consistent for API and MCP consumers.
- Prevents silent drift where one transport loses debuggability.

## Consequences
### Positive
- Better cross-channel debugging and incident triage.
- Stronger external contract clarity for integrators.

### Tradeoffs
- MCP error payload shape evolves (requires version-aware client communication).
- Additional test matrix and contract maintenance overhead.

## Guardrails
- Error-code stability remains separate and must not regress.
- Correlation IDs should avoid high-cardinality baggage beyond opaque trace tokens.
- Any transport-specific relaxation requires a superseding ADR.
