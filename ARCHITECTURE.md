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

## Layered design
- **api**: REST controllers + global error mapping (RFC7807-style)
- **application**: `TaskCommandService`, `TaskQueryService`, `TaskStore` seam
- **domain**: task model, transition policy, domain exceptions
- **infrastructure**:
  - in-memory store (default when `task.store` is absent)
  - Mongo store (`task.store=mongo`) with Micronaut Data documents/repositories

## Adapter readiness matrix
- REST v1: **implemented**
  - `POST /v1/tasks` *(Idempotency-Key required)*
  - `GET /v1/tasks/{id}`
  - `GET /v1/tasks?status=`
  - `PATCH /v1/tasks/{id}/status` *(Idempotency-Key required)*
- MCP v1 tools: **not implemented yet**
  - planned: `create_task`, `get_task`, `list_tasks`, `update_task_status`

## Error contract
Response body fields: `type`, `title`, `status`, `detail`, `instance`, `code`, `correlationId`.

Current mapped codes:
- `TASK_NOT_FOUND` (404)
- `INVALID_TASK_TRANSITION` (409)
- `CONCURRENT_MODIFICATION` (409)
- `TASK_CONFLICT` (409; generic conflict bucket, keep usage narrow)
- `VALIDATION_FAILED` (400)
- `BAD_REQUEST` (400)
- `INTERNAL_ERROR` (500)

Planned additional code:
- `IDEMPOTENCY_KEY_REUSE_MISMATCH` (409)

`X-Correlation-Id` is echoed from request or generated when missing.

## Mongo implementation status
Implemented:
- `TaskDocument` with `@Version` optimistic locking
- `MongoTaskStore` adapter + repositories
- startup index initializer currently creates:
  - `tasks`: `{ status: 1, updatedAt: -1 }`
  - `tasks`: `{ updatedAt: -1 }`
  - `idempotency_records`: unique `{ key: 1 }`
  - `idempotency_records`: TTL on `createdAt` (hours-based)

Open gaps before MVP GA:
- DB/collection targeting in index initializer currently hardcoded (`agent_tracker`)
- idempotency schema/indexes are not yet ADR-compliant (`(operation,key)`, `payloadHash`, `expiresAt`)
- idempotency mismatch rejection (`IDEMPOTENCY_KEY_REUSE_MISMATCH`) not implemented
- MCP adapter + parity suite absent
- OpenAPI snapshot/diff governance absent

## Current architectural focus (next)
1. Implement idempotency v2 rollout (ADR-005): dual-read compatibility, v2 writes, then cleanup.
2. Deliver MCP tools through shared services only.
3. Add parity and contract-governance gates in CI (REST↔MCP scenarios + OpenAPI drift check).
