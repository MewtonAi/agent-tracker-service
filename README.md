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
1. MCP runtime wire-level `tools/list` smoke check against live HTTP transport (MVP gate)
2. Tighten OpenAPI drift gate from marker checks to strict generated-vs-checked-in comparison (MVP gate)
3. Post-MVP: promote idempotency observability markers to metrics dashboards/alerts

Gate semantics are codified in `ADR-008-mvp-gate-tightening-for-mcp-transport-and-openapi-drift.md`.

Migration posture note: idempotency semantics are v2-only in this repo lineage (see ADR-007).

## Local validation
Run:
```bash
./gradlew check
```

OpenAPI contract workflow:
```bash
./gradlew updateOpenApiSnapshot
./gradlew verifyOpenApiSnapshot
```

MCP runtime transport/registration smoke:
```bash
./gradlew test --tests "*TaskMcpRuntimeTransportContractTest"
```
