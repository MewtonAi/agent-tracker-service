# Agent Tracker Service Architecture (v1)

Last updated: 2026-02-24 (PST, ADR-023 signal-status normalization refresh)

## Purpose
Task-first system of record for agent work tracking, exposed via REST and MCP with shared business semantics.

## Canonical v1 decisions
- Entity/API scope: **Task only** (Project APIs deferred; project model artifacts may remain internal/non-routable)
- Canonical lifecycle: `NEW -> IN_PROGRESS -> {BLOCKED|DONE|CANCELED}`, `BLOCKED -> {IN_PROGRESS|CANCELED}`
- Mutations require idempotency key (`Idempotency-Key` in REST; equivalent field in MCP tools)
- REST and MCP adapters share application services (no transport-specific business rules)
- Optimistic-write conflicts map to stable code: `CONCURRENT_MODIFICATION` (HTTP 409)
- Idempotency mismatch maps to stable code: `IDEMPOTENCY_KEY_REUSE_MISMATCH` (HTTP 409)
- List pagination contract is parity-aligned across REST/MCP (`limit`, `cursor`, `nextCursor`) via `ADR-013-task-list-pagination-ordering-contract.md`

## Layered design
- **api**: REST controllers + global error mapping (RFC7807-style)
- **application**: `TaskCommandService`, `TaskQueryService`, `TaskStore` seam, `IdempotencyTelemetry`
- **domain**: task model, transition policy, domain exceptions
- **infrastructure**:
  - in-memory store (default when `task.store` is absent)
  - Mongo store (`task.store=mongo`) with Micronaut Data documents/repositories
  - logging telemetry baseline (`LoggingIdempotencyTelemetry`)
- **application.contract**:
  - shared `TaskInputNormalizer` for transport-agnostic input parsing/validation used by REST + MCP adapters and query service
- **mcp adapter**:
  - `TaskMcpTools` (`createTask`, `getTask`, `listTasks`, `updateTaskStatus`)
  - transport/runtime registration contract tested via HTTP JSON-RPC `initialize` + `tools/list`

## Runtime profile baseline
- `local`: developer defaults (`task.store=memory`; local Mongo/idempotency overrides via env)
- `test`: deterministic checks (`task.store=memory`, idempotency TTL = 1h, management endpoints disabled)
- `prod`: runtime posture (`task.store=mongo`, `MONGODB_URI` required, management endpoints restricted)
- Detailed key matrix and activation examples: `docs/config-profile-matrix.md`

## Adapter readiness matrix
- REST v1: **implemented**
  - `POST /v1/tasks` *(Idempotency-Key required)*
  - `GET /v1/tasks/{id}`
  - `GET /v1/tasks?status=&cursor=&limit=`
  - `PATCH /v1/tasks/{id}/status` *(Idempotency-Key required)*
- MCP v1 task tools: **implemented and contract-gated**
  - code-level registration/schema checks (`TaskMcpToolRegistrationContractTest`)
  - runtime transport-wire handshake/discovery checks (`TaskMcpRuntimeTransportContractTest`)
- REST/MCP parity scenarios: **implemented and CI-gated** (`TaskRestMcpParityTest`)

## Error contract
Response body fields: `type`, `title`, `status`, `detail`, `instance`, `code`, `correlationId`.

Current mapped codes:
- `TASK_NOT_FOUND` (404)
- `INVALID_TASK_TRANSITION` (409)
- `CONCURRENT_MODIFICATION` (409)
- `IDEMPOTENCY_KEY_REUSE_MISMATCH` (409)
- `TASK_CONFLICT` (409; generic conflict bucket, keep usage narrow)
- `VALIDATION_FAILED` (400)
- `BAD_REQUEST` (400)
- `INTERNAL_ERROR` (500)

`X-Correlation-Id` is echoed from REST request header or generated when missing.
MCP correlation policy (`ADR-012-mcp-correlation-id-canonicalization-policy.md`): tool-request `correlationId` is propagated when it is a valid UUID; UUID fallback is generated when absent/blank/invalid.

## Contract governance
- `verifyOpenApiSnapshot` enforces strict generated-vs-checked-in equality for `openapi/openapi.yaml`.
- Marker assertions remain as an additional guard for critical routes and error codes.
- CI workflow gate runs `./gradlew check` on push/PR.
- ADR source-of-truth governance follows `ADR-014-contract-source-of-truth-and-supersession-policy.md` (superseded ADRs are historical, not active policy).
- Cursor token evolution governance follows `ADR-015-cursor-token-evolution-and-backward-compatibility.md` (cursor remains externally opaque across token format changes).
- Release go/no-go evidence bundle policy follows `ADR-016-release-readiness-evidence-and-go-no-go-gate.md`.
- Release evidence artifact location and PR template policy follow `ADR-017-release-evidence-artifact-and-pr-template-policy.md` (`docs/release-evidence.md`, `.github/pull_request_template.md`).
- Release-candidate sequencing and temporary feature-freeze posture follow `ADR-018-release-candidate-readiness-lanes-and-feature-freeze-policy.md`.
- Release evidence provenance + freshness requirements follow `ADR-019-release-evidence-provenance-and-freshness-policy.md` (source declaration, head-SHA parity, <=24h freshness window).
- Release-policy documentation contract coverage follows `ADR-020-release-contract-documentation-coverage-policy.md` (canonical ADR-set checks extend to planning + evidence artifacts).
- Java 21 verification provenance-ladder policy follows `ADR-021-java21-toolchain-readiness-and-verification-provenance-ladder.md` (local-preferred, CI fallback, mandatory preflight declaration in evidence artifacts).
- Release evidence minimum test-signal policy follows `ADR-022-release-evidence-minimum-test-signal-set-policy.md` (required signal declarations for parity/runtime/OpenAPI/docs-contract checks).
- Release evidence signal-status normalization policy follows `ADR-023-release-evidence-signal-status-normalization-and-no-go-default.md` (required statuses: `PASS`/`FAIL`/`NOT_RUN`; `FAIL` or `NOT_RUN` keeps decision at `NO-GO`).

## Mongo implementation status
Implemented:
- `TaskDocument` with `@Version` optimistic locking
- `MongoTaskStore` adapter + repositories
- startup index initializer creates:
  - `tasks`: `{ status: 1, updatedAt: -1, taskId: -1 }`
  - `tasks`: `{ updatedAt: -1, taskId: -1 }`
  - `idempotency_records`: unique `{ operation: 1, key: 1 }`
  - `idempotency_records`: TTL on `expiresAt`
- idempotency TTL retention configurable (`idempotency.ttl-hours`, default 48h)
- migration posture: v2-only idempotency semantics; no legacy key-only replay fallback (ADR-007)
- idempotency observability markers emitted (`idempotency.first_write`, `idempotency.replay_hit`, `idempotency.mismatch_reject`) with operation dimension

## Active architectural focus (post-MVP)
1. **Lane 0 completion (ADR-018):** OpenAPI snapshot reconciliation on Java 21 and CI green evidence.
2. **Lane 1 completion (ADR-018 + ADR-019 + ADR-023):** release-evidence closure in PR + handoff with explicit GO/NO-GO statement, normalized signal statuses, provenance source, and freshness validation.
3. **Lane 2 hardening (ADR-018):** cursor evolution planning and broader platform hardening after lane closure.
4. Continue API surface hygiene so deferred project artifacts remain internal-only and non-routable.
