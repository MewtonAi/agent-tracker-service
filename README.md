## Agent Tracker Service (MVP)

Current implementation snapshot:
- Canonical v1 Task lifecycle aligned across code/docs (`NEW`, `IN_PROGRESS`, `BLOCKED`, `DONE`, `CANCELED`)
- Task-first scope (Project APIs deferred)
- Application services:
  - `TaskCommandService` (create, update status, idempotency)
  - `TaskQueryService` (get, list by optional status)
- REST v1 endpoints:
  - `POST /v1/tasks` *(Idempotency-Key required)*
  - `GET /v1/tasks/{id}`
  - `GET /v1/tasks?status=`
  - `PATCH /v1/tasks/{id}/status` *(Idempotency-Key required)*
- RFC7807-style API error contract with stable `code` and `correlationId`
- Concurrency conflict code locked for optimistic-write races: `CONCURRENT_MODIFICATION`
- Status update idempotency is scoped by `(taskId, idempotencyKey)` to avoid cross-task replay collisions
- Mongo idempotency TTL retention is configurable via `idempotency.ttl-hours` / `IDEMPOTENCY_TTL_HOURS`

## Next priorities
1. Durable idempotency v2: `(operation,key)` uniqueness + payload hash mismatch policy (`IDEMPOTENCY_KEY_REUSE_MISMATCH`)
2. MCP tool delivery (`create_task`, `get_task`, `list_tasks`, `update_task_status`) + REST/MCP parity suite
3. OpenAPI generation/check-in + CI contract drift gate
4. Concurrency integration test path for deterministic 409 under contested writes

## Local validation
Run:
```bash
./gradlew test
```

> In this run, local test execution could not be completed in the environment because `JAVA_HOME`/`java` was unavailable.
