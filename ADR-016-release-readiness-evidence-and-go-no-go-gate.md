# ADR-016: Release-readiness evidence bundle and go/no-go gate

- Status: Accepted
- Date: 2026-02-24
- Deciders: product/architecture
- Related: `ADR-008-mvp-gate-tightening-for-mcp-transport-and-openapi-drift.md`, `ADR-014-contract-source-of-truth-and-supersession-policy.md`, `.github/workflows/ci.yml`

## Context
REST + MCP parity is now implemented and heavily contract-tested, but release confidence is still vulnerable to process drift:
- OpenAPI snapshot drift is easy to miss when Java 21 is unavailable locally.
- Evidence of parity/readiness is spread across CI logs and ad-hoc notes.
- Go/no-go calls can become subjective when required verification artifacts are not standardized.

## Decision
For each release candidate, maintain a single evidence bundle (in PR description and handoff notes) containing all of the following:
1. `./gradlew check` passed on JDK 21 (CI run URL + commit SHA).
2. `verifyOpenApiSnapshot` passed and `openapi/openapi.yaml` is either unchanged or intentionally updated in the same PR.
3. REST/MCP parity tests are present in the `check` run (no selective test bypass for release branches).
4. Active contract references in release notes/handoff point only to canonical ADR files per ADR-014.

## Consequences
### Positive
- Go/no-go decisions become deterministic and auditable.
- Reduces regressions caused by undocumented local environment constraints.
- Improves handoff quality between architecture and implementation slices.

### Tradeoffs
- Slightly more release hygiene overhead per PR.
- Requires discipline in PR templates/checklists to stay effective.

## Follow-up
- Implemented by ADR-017 with repository artifacts:
  - `docs/release-evidence.md`
  - `.github/pull_request_template.md`
- Keep template headings synchronized when evidence fields evolve.