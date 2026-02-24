# Agent Tracker Service Architecture (Micronaut + MongoDB + MCP)

## 1) Purpose
System of record for agent task lifecycle with dual access patterns:
- REST for service/dashboard integration
- MCP tools for AI-native orchestration

## 2) Current state (implemented)
- Micronaut 4 + Java 21 modular monolith.
- REST controller for task create/get/list/status + assign/unassign.
- In-memory application service (`TaskLifecycleService`).
- Transition policy enforced in domain layer.
- API error mapping and baseline tests.

## 3) Canonical v1 domain boundary
- v1 focus: **Task lifecycle only**.
- `projectId` is a reference field on tasks (no project aggregate API in v1).

### Canonical task statuses
`BACKLOG, READY, IN_PROGRESS, BLOCKED, IN_REVIEW, DONE, CANCELLED`

### Transition policy
- BACKLOG -> READY | CANCELLED
- READY -> BACKLOG | IN_PROGRESS | CANCELLED
- IN_PROGRESS -> READY | BLOCKED | IN_REVIEW | CANCELLED
- BLOCKED -> IN_PROGRESS | CANCELLED
- IN_REVIEW -> IN_PROGRESS | DONE | CANCELLED
- DONE -> terminal
- CANCELLED -> terminal

## 4) Target architecture for MVP readiness

### Inbound adapters
- REST `/v1/tasks/*`
- MCP tools: `create_task`, `get_task`, `list_tasks`, `update_task_status`

### Application layer
- Shared command/query services used by both REST and MCP.
- Centralized validation and idempotency coordination.

### Domain layer
- Task entity/value objects + transition policy (single source of truth).
- Domain exceptions mapped consistently by adapter layer.

### Outbound adapters
- Mongo `tasks` persistence with optimistic locking.
- Optional (P1): `task_events` append-only audit stream.

## 5) Persistence design (MVP)
Collection: `tasks`
Core fields:
- `_id`, `taskId`, `projectId`, `title`, `description`
- `taskType`, `status`, `priority`, `assignee`
- `audit` (createdAt/by, updatedAt/by)
- `idempotencyKey` (or dedicated idempotency collection)
- `version`

Required indexes:
- Unique: idempotency/external mutation key
- Compound: `{ status: 1, priority: -1, audit.updatedAt: -1 }`
- Compound: `{ projectId: 1, audit.updatedAt: -1 }`

## 6) Contract and error model
- REST uses versioned path `/v1` and problem-details style errors.
- Stable `code` and `correlationId` required on error responses.
- MCP maps to stable errors: `INVALID_ARGUMENT`, `NOT_FOUND`, `CONFLICT`, `INTERNAL`.

## 7) Key tradeoffs / decisions
- Modular monolith over microservices for speed and simplicity.
- Mongo chosen for flexible metadata evolution.
- Shared service layer mandated to prevent REST/MCP semantic drift.

## 8) Immediate architecture work items
1. Replace in-memory map with Mongo adapter behind existing contract.
2. Add idempotent mutation handling and optimistic locking.
3. Implement MCP tool adapter and parity test suite.
4. Generate and lock OpenAPI as source-of-truth REST contract.
