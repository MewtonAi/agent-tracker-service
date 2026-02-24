# Agent Tracker Service — Domain Model (v1 canonical)

## Scope
v1 models and APIs are task-centric.
- In scope: task lifecycle, assignment, audit metadata.
- Out of scope in v1: project aggregate CRUD.

## Core entity
### `Task`
Fields:
- `taskId`
- `projectId` (reference only)
- `title`, `description`
- `taskType`
- `status`
- `priority`
- `assignee` (`AgentRef`, nullable)
- `audit` (`AuditMetadata`)

### `AgentRef`
- `agentId`
- `displayName`
- `capabilities`

### `AuditMetadata`
- `createdAt`, `createdBy`
- `updatedAt`, `updatedBy`

## Enums (canonical)
- `TaskType`: FEATURE, BUG, CHORE, INCIDENT, RESEARCH, DOCS, TEST
- `TaskStatus`: BACKLOG, READY, IN_PROGRESS, BLOCKED, IN_REVIEW, DONE, CANCELLED
- `TaskPriority`: LOW, MEDIUM, HIGH, CRITICAL

## Lifecycle and invariants
- Status transitions are validated by `TaskTransitionPolicy`.
- `DONE` and `CANCELLED` are terminal states.
- Assignment should be blocked for terminal states (P1 hardening; currently not enforced).
- `created*` fields are immutable.
- `updated*` fields change on every successful mutation.

## Domain contracts (current)
- `TaskLifecycleContract`
  - create task
  - update status
  - assign task
  - unassign task
  - get task by id
  - list tasks by project

## v1 readiness gaps (domain/application)
- Add idempotency semantics for mutating operations.
- Add versioned concurrency control (optimistic locking).
- Ensure assignment invariants for terminal states.
- Keep status transition matrix as a single source of truth across REST + MCP.
