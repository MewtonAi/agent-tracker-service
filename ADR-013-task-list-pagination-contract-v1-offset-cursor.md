# ADR-013: Task List Pagination Contract (v1 Offset Cursor)

- **Status:** Accepted
- **Date:** 2026-02-24
- **Deciders:** product/architecture
- **Related:** `TaskQueryService`, `TaskController`, `TaskMcpTools`, `TaskRestMcpParityTest`

## Context
Task list APIs/tools previously returned unbounded result sets.

A parity-safe pagination contract has now been implemented across REST and MCP:
- request inputs: `limit`, `cursor`
- response envelope: `tasks`, `nextCursor`

## Decision
1. **v1 cursor form:** `cursor` is an opaque string but currently encodes a non-negative integer offset.
2. **Limit policy:** default limit is 50; valid range is `1..200`.
3. **Ordering contract:** list order is deterministic and aligned in stores (`updatedAt DESC`, `taskId ASC` tie-break).
4. **Terminal page behavior:** when no additional items remain, `nextCursor = null`.
5. **Compatibility:** callers omitting pagination fields retain first-page behavior.

## Consequences
### Positive
- REST + MCP list behavior is bounded and parity-testable.
- Existing clients can adopt pagination incrementally.

### Tradeoffs
- Offset-backed cursor can degrade for large datasets and mutation-heavy timelines.
- Current query path paginates after loading all filtered tasks from store.

## Follow-up (required)
- Introduce store-level seek pagination for Mongo-backed reads while preserving external cursor compatibility.
- Keep cursor external contract opaque so implementation can evolve without breaking clients.
