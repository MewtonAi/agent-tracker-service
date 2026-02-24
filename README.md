## Agent Tracker Service (MVP)

Current implementation snapshot (inspected 2026-02-24 PST):
- Canonical v1 Task lifecycle aligned across code/docs (`NEW`, `IN_PROGRESS`, `BLOCKED`, `DONE`, `CANCELED`)
- Task-first scope (Project APIs deferred)
- Application services:
  - `TaskCommandService` (create, update status, idempotency)
  - `TaskQueryService` (get, list by optional status)
- MCP tool service surface (`TaskMcpTools`): `createTask`, `getTask`, `listTasks`, `updateTaskStatus`
- REST v1 endpoints:
  - `POST /v1/tasks` *(Idempotency-Key required)*
  - `GET /v1/tasks/{id}`
  - `GET /v1/tasks?status=&cursor=&limit=`
  - `PATCH /v1/tasks/{id}/status` *(Idempotency-Key required)*
- RFC7807-style API error contract with stable `code` and `correlationId`
- Concurrency conflict code locked for optimistic-write races: `CONCURRENT_MODIFICATION`
- Status update idempotency is scoped by `(taskId, idempotencyKey)` to avoid cross-task replay collisions
- Mongo idempotency v2 baseline: unique `(operation,key)`, payload hash mismatch detection, TTL on `expiresAt`
- Mongo index manifest + startup verification contract: `docs/mongo-index-manifest.md` (`event=mongo_index_state`, integration-gated)
- Mismatch contract is enforced as HTTP 409 with `IDEMPOTENCY_KEY_REUSE_MISMATCH`
- Mongo idempotency TTL retention is configurable via `idempotency.ttl-hours` / `IDEMPOTENCY_TTL_HOURS` (default 48h)
- MCP runtime transport handshake/discovery is contract-gated (`initialize` + `tools/list`)
- OpenAPI drift is contract-gated via strict generated-vs-snapshot equality
- Durable idempotency metrics are exported via Micrometer/Prometheus as:
  - `agent_tracker_idempotency_events_total{event,operation}` where `event ∈ {first_write,replay_hit,mismatch_reject}`

## Current priorities
1. Reconcile OpenAPI snapshot with shipped pagination/correlation contract fields (`./gradlew updateOpenApiSnapshot && ./gradlew check`)
2. Apply release-evidence workflow in PR/handoff artifacts using `docs/release-evidence.md` and `.github/pull_request_template.md`
3. Finalize ADR/source-of-truth hygiene (canonical references only; superseded ADRs remain historical)
4. Enforce release evidence provenance/freshness (ADR-019) in PR + handoff artifacts
5. Implement cursor evolution readiness (dual-decode compatibility) under ADR-015 while keeping `limit/cursor/nextCursor` stable; track phase-2 plan in `docs/cursor-evolution-phase2-plan.md`

Gate semantics are codified in `ADR-008-mvp-gate-tightening-for-mcp-transport-and-openapi-drift.md`.
Migration posture note: idempotency semantics are v2-only in this repo lineage (see ADR-007).
Canonical contract ADR references are governed by ADR-014 (superseded ADR variants are historical only). Cursor evolution compatibility policy is defined in ADR-015. Release go/no-go evidence requirements are defined in ADR-016, artifact/template enforcement is defined in ADR-017 (`docs/release-evidence.md`, `.github/pull_request_template.md`), release-candidate lane sequencing/temporary feature-freeze posture is defined in ADR-018, evidence provenance/freshness policy is defined in ADR-019, documentation contract-coverage policy is defined in ADR-020, Java-21 preflight/verification-source policy is defined in ADR-021, minimum release test-signal declaration policy is defined in ADR-022, and normalized signal-status/NO-GO default policy is defined in ADR-023. Sequenced execution guidance lives in `docs/rest-mcp-readiness-roadmap.md`.

## Runtime profiles
Baseline profiles are defined in:
- `src/main/resources/application-local.yml`
- `src/main/resources/application-test.yml`
- `src/main/resources/application-prod.yml`
- matrix doc: `docs/config-profile-matrix.md`

Activation examples:
```bash
MICRONAUT_ENVIRONMENTS=local ./gradlew run
MICRONAUT_ENVIRONMENTS=test ./gradlew check
MICRONAUT_ENVIRONMENTS=prod java -jar build/libs/agent-tracker-service-*.jar
```

## Local validation
Preflight:
```bash
java -version
echo $JAVA_HOME
```
If Java 21 is unavailable locally, run verification via CI (`ci-java21`) and record that source in `docs/release-evidence.md` per ADR-021.

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
