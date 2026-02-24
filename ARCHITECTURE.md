# Agent Tracker Service Architecture (v1)

Last updated: 2026-02-24 (PST)

## Purpose
Task-first system of record for agent work tracking, exposed via REST and prepared for MCP parity.

## Canonical v1 decisions
- Entity/API scope: **Task only** (Project APIs deferred; project model artifacts may remain internal/non-routable)
- Canonical lifecycle: `NEW -> IN_PROGRESS -> {BLOCKED|DONE|CANCELED}`, `BLOCKED -> {IN_PROGRESS|CANCELED}`
- Mutations require idempotency key (`Idempotency-Key` in REST; equivalent field in MCP tools)
- REST and MCP adapters share application services (no transport-specific business rules)
- Optimistic-write conflicts map to stable code: `CONCURRENT_MODIFICATION` (HTTP 409)
- Idempotency mismatch maps to stable code: `IDEMPOTENCY_KEY_REUSE_MISMATCH` (HTTP 409)

## Layered design
- **api**: REST controllers + global error mapping (RFC7807-style)
- **application**: `TaskCommandService`, `TaskQueryService`, `TaskStore` seam, `IdempotencyTelemetry`
- **domain**: task model, transition policy, domain exceptions
- **infrastructure**:
  - in-memory store (default when `task.store` is absent)
  - Mongo store (`task.store=mongo`) with Micronaut Data documents/repositories
  - logging telemetry baseline (`LoggingIdempotencyTelemetry`)

## Adapter readiness matrix
- REST v1: **implemented**
  - `POST /v1/tasks` *(Idempotency-Key required)*
  - `GET /v1/tasks/{id}`
  - `GET /v1/tasks?status=`
  - `PATCH /v1/tasks/{id}/status` *(Idempotency-Key required)*
- MCP v1 tool service surface: **implemented in application adapter** (`TaskMcpTools`)
  - methods: `createTask`, `getTask`, `listTasks`, `updateTaskStatus`
  - code-level surface/schema contract tests are in place
  - runtime transport-level registration/schema verification remains pending

## Error contract
Response body fields: `type`, `title`, `status`, `detail`, `instance`, `code`, `correlationId`.

Current mapped codes:
- `TASK_NOT_FOUND` (404)
- `INVALID_TASK_TRANSITION` (409)
- `CONCURRENT_MODIFICATION` (409)
- `IDEMPOTENCY_KEY_REUSE_MISMATCH` (409)
- `TASK_CONFLICT` (409; generic conflict bucket, keep usage narrow)
- `VALIDATION_FAILED` (400)
- `BAD_REQUEST` (400)
- `INTERNAL_ERROR` (500)

`X-Correlation-Id` is echoed from request or generated when missing.

## Mongo implementation status
Implemented:
- `TaskDocument` with `@Version` optimistic locking
- `MongoTaskStore` adapter + repositories
- startup index initializer creates:
  - `tasks`: `{ status: 1, updatedAt: -1 }`
  - `tasks`: `{ updatedAt: -1 }`
  - `idempotency_records`: unique `{ operation: 1, key: 1 }`
  - `idempotency_records`: TTL on `expiresAt`
- idempotency TTL retention configurable (`idempotency.ttl-hours`, default 48h)
- migration posture: v2-only idempotency semantics; no legacy key-only replay fallback in current repo lineage (ADR-007)
- idempotency observability markers emitted (`idempotency.first_write`, `idempotency.replay_hit`, `idempotency.mismatch_reject`) with operation dimension

Remaining MVP-critical gaps:
- parity tests are not yet wired as an explicit CI release gate
- MCP runtime registration/schema verification not yet wired
- OpenAPI snapshot/diff governance absent

## Current architectural focus (next)
1. Wire REST↔MCP parity scenarios as required CI gate.
2. Add MCP runtime smoke verification (discoverability/schema) and enforce it pre-merge.
3. Add OpenAPI snapshot + drift check to lock externally visible REST contract.
4. Post-MVP: promote idempotency markers to metrics dashboards/alerts.
