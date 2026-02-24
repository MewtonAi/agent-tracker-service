# Agent Tracker Service — Domain Model (v1 Canonical)

## v1 Scope Decision (Canonical)

- **In scope (v1):** `Task` lifecycle only (task-first MVP)
- **Deferred:** `Project` lifecycle APIs/entities
- **Primary write operations:** create task, update task status
- **Primary read operations:** get task by id, list tasks (optional status filter)

## Task Entity

Key fields:
- `taskId` (server-generated)
- `title` (required)
- `description` (optional)
- `taskType` (optional, default `FEATURE`)
- `status` (server-managed lifecycle)
- `priority` (optional, default `MEDIUM`)
- `assignee` (optional)
- `audit` (`createdAt/By`, `updatedAt/By`)

## Enumerations

- `TaskType`: FEATURE, BUG, CHORE, INCIDENT, RESEARCH, DOCS, TEST
- `TaskPriority`: LOW, MEDIUM, HIGH, CRITICAL
- `TaskStatus` (canonical v1):
  - `NEW`
  - `IN_PROGRESS`
  - `BLOCKED`
  - `DONE`
  - `CANCELED`

## Transition Matrix (v1)

| From        | Allowed To                         |
|-------------|------------------------------------|
| NEW         | IN_PROGRESS, CANCELED              |
| IN_PROGRESS | BLOCKED, DONE, CANCELED            |
| BLOCKED     | IN_PROGRESS, CANCELED              |
| DONE        | *(none)*                           |
| CANCELED    | *(none)*                           |

Invalid transitions must return conflict semantics (`409` in REST).

## Required vs Optional Inputs (v1)

### Create task
- Required: `title`, `requestedBy`, `Idempotency-Key`
- Optional: `description`, `taskType`, `priority`

### Update task status
- Required: `status`, `requestedBy`, `Idempotency-Key`
- Optional: none

## Idempotency Contract

- `POST /v1/tasks` and `PATCH /v1/tasks/{id}/status` require `Idempotency-Key` header.
- Replays with the same key must return the same logical result without creating duplicates.
