# ADR-012: MCP Correlation ID Precedence and Fallback (Final v1 Policy)

- **Status:** Accepted
- **Date:** 2026-02-24
- **Deciders:** product/architecture
- **Supersedes:** `ADR-011-mcp-correlation-id-source-policy.md`
- **Related:** `ADR-010-cross-transport-correlation-id-contract.md`, `ARCHITECTURE.md`, `TaskMcpTools`

## Context
ADR-011 established an interim server-generated-only policy for MCP error correlation IDs.

The current MCP tool contract now includes optional `correlationId` on requests, and runtime behavior supports caller-provided values.

## Decision
1. **Canonical MCP source (v1):** tool request field `correlationId` is the primary source.
2. **Fallback rule:** when missing/blank, MCP generates a UUID and attaches it to tool errors.
3. **Stability rule:** mapped MCP error `code` values remain unchanged; only correlation source semantics are finalized.
4. **Parity rule:** this closes the caller-correlation parity gap with REST request header echo behavior (same intent, transport-specific field name).

## Consequences
### Positive
- Deterministic behavior for MCP clients.
- Better trace stitching across clients/logs while preserving non-empty fallback guarantees.

### Tradeoffs
- Correlation propagation currently relies on per-tool payload field, not transport-level metadata.
- Future metadata-level correlation conventions may require a new ADR and migration path.

## Guardrails
- Contract tests must continue to verify both:
  - caller-provided correlation propagation
  - generated fallback when absent
- Docs/backlog must treat ADR-011 as superseded and reference this ADR as active policy.
