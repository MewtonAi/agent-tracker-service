# Mongo Index Manifest

Version: v1 (2026-02-24)
Owner: Platform / Infrastructure

This manifest is the canonical list of required Mongo indexes for `task.store=mongo`.

## Required indexes

| Collection | Index name | Keys | Options | Owner | Notes |
|---|---|---|---|---|---|
| `tasks` | `tasks_status_updatedAt_taskId_idx` | `{ status: 1, updatedAt: -1, taskId: -1 }` | none | Task query path | Supports status-filtered list ordering contract (`updatedAt desc, taskId desc`). |
| `tasks` | `tasks_updatedAt_taskId_idx` | `{ updatedAt: -1, taskId: -1 }` | none | Task query path | Supports unfiltered list ordering contract. |
| `idempotency_records` | `idempotency_records_operation_key_uk` | `{ operation: 1, key: 1 }` | `unique=true` | Idempotency contract | Enforces replay-key uniqueness boundary per operation. |
| `idempotency_records` | `idempotency_records_expiresAt_ttl_idx` | `{ expiresAt: 1 }` | `expireAfterSeconds=0` | Idempotency retention | TTL cleanup; retention window set by `idempotency.ttl-hours`. |

## Startup verification contract

At startup, `MongoIndexInitializer` ensures all required indexes exist and emits one structured line per index:

- `event=mongo_index_state`
- `db=<database>`
- `collection=<collection>`
- `index=<indexName>`
- `outcome=<created|existing>`
- `keys=<json>`
- `unique=<true|false>`
- `ttlSeconds=<number|null>`

## Automated verification

`MongoTaskStoreIntegrationTest.shouldProvisionRequiredMongoIndexesFromManifest` asserts that all manifest indexes exist with matching keys/options. Any manifest drift fails `./gradlew check`.

## Change policy

- Additive index changes may be introduced by updating this manifest + initializer + tests in one PR.
- Destructive changes (drop/rename behavior) are **manual-gate only** and require a dedicated ADR/ticket.
