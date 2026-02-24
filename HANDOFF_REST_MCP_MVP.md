# Developer Handoff — REST + MCP Post-MVP Hardening

Last updated: 2026-02-24 (PST, developer implementation pass)

## What changed in this run
- Hardened pagination determinism by aligning sort tie-break contract to `updatedAt DESC, taskId DESC` across stores.
- Hardened MCP correlation propagation by canonicalizing caller-supplied `correlationId` as UUID and falling back to generated UUID when absent/blank/invalid.
- Added focused tests for query pagination bounds/validation and MCP invalid-correlation fallback.
- Added final ADRs:
  - `ADR-012-mcp-correlation-id-canonicalization-policy.md` (supersedes ADR-011)
  - `ADR-013-task-list-pagination-ordering-contract.md`
- Updated architecture docs for correlation semantics and Mongo sort-support indexes.

## Code changes summary
- `InMemoryTaskStore`: deterministic sort now uses `taskId DESC` tie-break.
- `TaskMongoRepository`/`MongoTaskStore`: method contracts switched to `...OrderByUpdatedAtDescTaskIdDesc`.
- `MongoIndexInitializer`: compound indexes now include `taskId` with `updatedAt`.
- `TaskMcpTools`: `resolveCorrelationId` now enforces UUID canonicalization with fallback.

## Test additions/updates
- Added `TaskQueryServiceTest`:
  - pagination + `nextCursor` terminal behavior
  - invalid `cursor`/`limit` bounds rejection
  - cursor past end returns empty page + `nextCursor=null`
- Updated `TaskRestMcpParityTest`:
  - UUID propagation assertion for caller-supplied correlation IDs
  - invalid correlation input fallback to generated UUID

## Environment caveat
- Local build/tests could not be executed in this shell (Java/JAVA_HOME unavailable).
- CI `./gradlew check` remains the authoritative gate.

## Suggested next slice
1. Run Java 21 local verification (`./gradlew check`) and refresh OpenAPI snapshot if drift exists.
2. Continue with store-level pagination optimization (seek/DB-backed) while preserving current external envelope contract.
