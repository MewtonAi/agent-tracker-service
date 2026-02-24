# ADR-015: Cursor token evolution and backward compatibility

- **Status:** Accepted
- **Date:** 2026-02-24
- **Deciders:** product/architecture
- **Related:** `ADR-013-task-list-pagination-ordering-contract.md`, `ARCHITECTURE.md`, `PRODUCT_OWNER_NEXT.md`

## Context
Pagination currently uses an opaque `cursor` field while the concrete MVP token is an integer offset. We need a safe path to evolve cursor internals (for seek-style paging) without breaking REST/MCP parity or existing clients.

## Decision
1. Cursor input remains externally opaque and must continue using the same request/response field names (`cursor`, `nextCursor`).
2. Decoding accepts both:
   - legacy offset token: `<n>`
   - prefixed offset token for forward compatibility: `o:<n>`
3. Validation guarantees remain unchanged:
   - non-numeric/negative values are rejected as bad request.
4. Emitted `nextCursor` remains legacy numeric offset in v1 for maximal compatibility.

## Consequences
### Positive
- Existing clients remain backward compatible.
- New token families can be introduced incrementally with dual-decode.
- REST/MCP parity remains contract-stable.

### Tradeoffs
- Decoder complexity increases slightly.
- Offset cursor performance characteristics are unchanged until seek token emission is introduced.
