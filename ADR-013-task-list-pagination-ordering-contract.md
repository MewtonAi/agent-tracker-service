# ADR-013: Task list pagination ordering contract

- Status: Accepted
- Date: 2026-02-24

## Context
Task listing is exposed through REST and MCP. Cursor pagination must produce deterministic, parity-safe pages across in-memory and Mongo stores.

## Decision
- Contract fields remain: `limit`, `cursor`, `nextCursor`.
- Sorting contract: `updatedAt DESC, taskId DESC`.
- Current cursor token remains offset-based for MVP (`cursor` is non-negative integer index).
- Invalid `limit` (`<1` or `>200`) and invalid `cursor` (non-integer/negative) are rejected as bad request.

## Consequences
- REST and MCP return equivalent pages under the same parameters.
- Tie-break on `taskId DESC` prevents non-deterministic ordering among same-timestamp rows.
- Mongo indexes include `taskId` with `updatedAt` to support the sort contract.
- A future seek-cursor strategy can replace offset tokening without changing envelope names.
