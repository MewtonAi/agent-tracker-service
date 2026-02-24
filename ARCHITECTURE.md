# Agent Tracker Service Architecture (v1)

## Purpose
Task-first system of record for agent work tracking, exposed via REST and designed for MCP parity.

## Canonical v1 decisions
- Entity scope: **Task only** (Project deferred)
- Canonical status lifecycle: `NEW -> IN_PROGRESS -> {BLOCKED|DONE|CANCELED}`, `BLOCKED -> {IN_PROGRESS|CANCELED}`
- Mutations require idempotency key
- REST and future MCP adapters must use shared application services

## Layers
- **api**: REST controllers + exception mapping
- **application**: `TaskCommandService`, `TaskQueryService`
- **domain**: task model + transition policy + domain exceptions
- **infrastructure (next)**: Mongo repositories + index bootstrapping + optimistic locking

## REST v1
- `POST /v1/tasks` (requires `Idempotency-Key`)
- `GET /v1/tasks/{id}`
- `GET /v1/tasks?status=`
- `PATCH /v1/tasks/{id}/status` (requires `Idempotency-Key`)

## Error contract
RFC7807-style response body:
- `type`, `title`, `status`, `detail`, `instance`, `code`, `correlationId`

Error mapping:
- not found -> `404 TASK_NOT_FOUND`
- invalid transition -> `409 INVALID_TASK_TRANSITION`
- validation/input -> `400 VALIDATION_FAILED` / `BAD_REQUEST`
- unexpected -> `500 INTERNAL_ERROR`

`X-Correlation-Id` is echoed from request (or generated) and returned on errors.

## Mongo readiness (next ticket)
- `tasks` collection with indexes on status/updatedAt and owner/status/updatedAt
- optimistic locking/version field
- unique idempotency tracking store
- persistence adapter parity tests (REST + MCP behavior equivalence)
