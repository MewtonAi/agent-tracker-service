# ADR-012: MCP correlationId canonicalization policy (final)

- Status: Accepted
- Date: 2026-02-24
- Supersedes: ADR-011 (interim source-only policy)

## Context
REST already guarantees `correlationId` on error paths. MCP now supports caller-provided `correlationId`, but parity requires a final rule for malformed values and stable representation.

## Decision
For MCP tool failures:
1. Read `correlationId` from the tool request payload.
2. If missing/blank, generate a new UUID.
3. If present, require UUID format (`UUID.fromString`).
4. If invalid, generate a new UUID fallback.
5. Emit the canonical UUID string on `McpToolException.correlationId`.

## Consequences
- Caller-provided UUIDs are propagated and remain traceable.
- Malformed caller values cannot poison observability dimensions.
- MCP and REST both guarantee non-empty correlation IDs on failures.
- Contract tests must cover: propagation, missing fallback, invalid fallback.
