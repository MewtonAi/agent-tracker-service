## Agent Tracker Service (MVP)

Current implementation snapshot:
- Canonical v1 Task lifecycle aligned across code/docs (`NEW`, `IN_PROGRESS`, `BLOCKED`, `DONE`, `CANCELED`)
- Task-first scope (Project APIs deferred)
- Application services:
  - `TaskCommandService` (create, update status, idempotency)
  - `TaskQueryService` (get, list by optional status)
- MCP tool service surface (`TaskMcpTools`): `createTask`, `getTask`, `listTasks`, `updateTaskStatus`
- REST v1 endpoints:
  - `POST /v1/tasks` *(Idempotency-Key required)*
  - `GET /v1/tasks/{id}`
  - `GET /v1/tasks?status=`
  - `PATCH /v1/tasks/{id}/status` *(Idempotency-Key required)*
- RFC7807-style API error contract with stable `code` and `correlationId`
- Concurrency conflict code locked for optimistic-write races: `CONCURRENT_MODIFICATION`
- Status update idempotency is scoped by `(taskId, idempotencyKey)` to avoid cross-task replay collisions
- Mongo idempotency v2 baseline: unique `(operation,key)`, payload hash mismatch detection, TTL on `expiresAt`
- Mismatch contract is enforced as HTTP 409 with `IDEMPOTENCY_KEY_REUSE_MISMATCH`
- Mongo idempotency TTL retention is configurable via `idempotency.ttl-hours` / `IDEMPOTENCY_TTL_HOURS` (default 48h)

## Next priorities
1. CI-enforced REST/MCP parity gate + MCP runtime registration/schema verification
2. OpenAPI generation/check-in + CI contract drift gate
3. Idempotency replay observability (`first_write`, `replay_hit`, `mismatch_reject`)

Migration posture note: idempotency semantics are v2-only in this repo lineage (see ADR-007).

## Local validation
Run:
```bash
./gradlew test
```

> In this run, local test execution could not be completed in the environment because `JAVA_HOME`/`java` was unavailable.
