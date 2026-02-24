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

## Next priorities
1. Mongo persistence adapter + indexes + optimistic locking
2. REST/MCP parity through shared service layer
3. OpenAPI generation/check-in pipeline

## Local validation
Run:
```bash
./gradlew test
```

> In this run, local test execution could not be completed in the environment because `JAVA_HOME`/`java` was unavailable.
