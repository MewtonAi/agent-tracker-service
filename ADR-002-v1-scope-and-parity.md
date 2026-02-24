# ADR-002: Canonical v1 Task Scope and REST/MCP Parity

- **Status:** Accepted
- **Date:** 2026-02-23
- **Deciders:** product/architecture
- **Related:** `ADR-001.md`, `ARCHITECTURE.md`, `DOMAIN_MODEL.md`, `PRODUCT_OWNER_NEXT.md`

## Context
Implementation includes working task REST endpoints and transition policy. Previous docs had conflicting lifecycle vocabularies, creating risk of contract drift and rework.

## Decision
1. **v1 scope is task-centric only.**
   - `projectId` remains a reference field; project aggregate/API is deferred.
2. **Canonical v1 task lifecycle is:**
   - `NEW, IN_PROGRESS, BLOCKED, DONE, CANCELED`.
3. **Canonical transition matrix:**
   - `NEW -> IN_PROGRESS | CANCELED`
   - `IN_PROGRESS -> BLOCKED | DONE | CANCELED`
   - `BLOCKED -> IN_PROGRESS | CANCELED`
   - `DONE` and `CANCELED` are terminal
4. **REST and MCP adapters must share one application service layer** for business rules.
5. **Parity is a release criterion:** equivalent outcomes/errors for equivalent command intent.

## Why
- Matches current implementation and test baseline (lowest rework path).
- Keeps MVP scope focused while preserving expansion room.
- Prevents transport-level semantic divergence.

## Consequences
### Positive
- Single authoritative lifecycle for contracts and tests.
- Clear implementation target for Mongo + MCP work.
- Faster MVP completion via reduced ambiguity.

### Tradeoffs
- Richer workflow states are deferred to future ADR/version.
- Introducing additional statuses later will require managed migration.

## Guardrails
- Lifecycle enum changes require ADR + contract/test updates.
- No adapter-specific business logic (REST/MCP remain thin).
- Conflict semantics standardized (HTTP 409 / MCP `CONFLICT`).

## Follow-up tickets
- Add durable idempotency + optimistic locking before GA.
- Add parity suite for create/get/list/update-status flows.
- Add post-v1 ADR for project aggregate if promoted.
