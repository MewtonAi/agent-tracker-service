# ADR-002: Canonical v1 Task Scope and REST/MCP Parity

- **Status:** Accepted
- **Date:** 2026-02-23
- **Deciders:** product/architecture
- **Related:** `ADR-001.md`, `ARCHITECTURE.md`, `PRODUCT_OWNER_NEXT.md`

## Context
Implementation now includes working task REST endpoints and transition policy. Existing documentation previously mixed two competing models:
1) task-only lifecycle view
2) project+task broader domain view

This ambiguity risks rework and inconsistent contracts between REST and MCP.

## Decision
1. **v1 scope is task-centric only.**
   - Project remains a referenced field (`projectId`) and not a first-class API aggregate in v1.
2. **Canonical task lifecycle is:**
   - `BACKLOG, READY, IN_PROGRESS, BLOCKED, IN_REVIEW, DONE, CANCELLED`.
3. **REST and MCP must share one application service layer** for all business rules.
4. **Parity is a release criterion:** equivalent behavior and errors for same command intent.

## Why
- Matches implemented code and tests today (lowest rework path).
- Keeps MVP focused while preserving project expansion in later phase.
- Prevents semantic drift between transport adapters.

## Consequences
### Positive
- Clear implementation target for Mongo + MCP work.
- Reduced contract ambiguity for downstream consumers.
- Faster MVP completion.

### Tradeoffs
- Project lifecycle APIs deferred despite domain presence.
- Some future migration effort if project aggregate becomes first-class.

## Guardrails
- Any lifecycle enum change requires ADR + contract/test update.
- No adapter-specific business rules (REST/MCP thin adapters only).
- Conflict semantics standardized: HTTP 409 / MCP `CONFLICT`.

## Follow-up tickets
- Add parity tests for create/get/list/update-status flows.
- Add idempotency + optimistic locking before GA.
- Add project aggregate as a separate post-v1 ADR if needed.
