# ADR-018: Release-candidate readiness lanes and feature-freeze policy

- Status: Accepted
- Date: 2026-02-24
- Deciders: product/architecture
- Related: `ADR-016-release-readiness-evidence-and-go-no-go-gate.md`, `ADR-017-release-evidence-artifact-and-pr-template-policy.md`, `.github/workflows/ci.yml`, `docs/release-evidence.md`

## Context
The project is close to REST + MCP MVP release readiness, but execution can drift when local Java 21 is unavailable:
- OpenAPI reconciliation and CI verification are blocked locally.
- Teams may start new scope before core release confidence is re-established.
- Handoff quality varies without a strict sequence for release-candidate slices.

## Decision
Adopt explicit readiness lanes with a temporary feature-freeze posture until Lane 1 is green.

### Lane 0 — Contract Integrity (mandatory first)
1. Run `./gradlew updateOpenApiSnapshot` on JDK 21.
2. Commit `openapi/openapi.yaml` if regenerated.
3. Run `./gradlew check` and capture CI evidence.

### Lane 1 — Release Evidence Closure (mandatory second)
1. Complete `docs/release-evidence.md` fields in PR/handoff.
2. Record CI URL + commit SHA + OpenAPI drift outcome.
3. Capture explicit GO/NO-GO statement with owner + timestamp.

### Lane 2 — Forward Hardening (allowed after Lane 1 green)
1. Cursor phase-2 token evolution planning/implementation under ADR-015.
2. Authn/authz and tenant-boundary work.
3. Outbox/eventing and retention lifecycle work.

### Feature-freeze rule
No new feature surface expansion (new endpoints/tools/contracts) is allowed while Lane 0 or Lane 1 remains open for the current release candidate.

## Consequences
### Positive
- Restores deterministic sequencing for release confidence work.
- Prevents release-risk work from being displaced by new scope.
- Improves cross-role handoff quality with repeatable evidence structure.

### Tradeoffs
- Short-term throughput on new features is reduced.
- Requires disciplined PR/handoff completion before hardening work begins.
