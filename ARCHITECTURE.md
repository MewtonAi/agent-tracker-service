# Agent Tracker Service Architecture (v1)

Last updated: 2026-02-23 (PST)

## Purpose
Task-first system of record for agent work tracking, exposed via REST and prepared for MCP parity.

## Canonical v1 decisions
- Entity scope: **Task only** (Project APIs deferred)
- Canonical lifecycle: `NEW -> IN_PROGRESS -> {BLOCKED|DONE|CANCELED}`, `BLOCKED -> {IN_PROGRESS|CANCELED}`
- Mutations require idempotency key (`Idempotency-Key`)
- REST and MCP adapters must share application services (no transport-specific business rules)

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
- `TASK_CONFLICT` (409; currently used for optimistic locking conflicts)
- `VALIDATION_FAILED` (400)
- `BAD_REQUEST` (400)
- `INTERNAL_ERROR` (500)

`X-Correlation-Id` is echoed from request or generated when missing.

## Mongo implementation status
Implemented:
- `TaskDocument` with `@Version` optimistic locking
- `MongoTaskStore` adapter + repositories
- startup index initializer (currently creates):
  - `tasks`: `{ status: 1, updatedAt: -1 }`
  - `tasks`: `{ priority: 1, updatedAt: -1 }`
  - `idempotency_records`: unique `{ key: 1 }`

Known gaps to close before MVP GA:
- configurable DB/collection targeting in index initializer (currently hardcoded DB name)
- idempotency record schema lacks operation dimension + payload fingerprint + TTL expiry
- conflict error code should be finalized (`TASK_CONFLICT` vs `CONCURRENT_MODIFICATION`) and locked in tests/docs
- invalid status filter behavior should be normalized to stable API error shape (avoid enum parsing leakage)
- MCP adapter + parity suite absent
