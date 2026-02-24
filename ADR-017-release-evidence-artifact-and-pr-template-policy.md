# ADR-017: Release evidence artifact and PR template policy

- Status: Accepted
- Date: 2026-02-24
- Deciders: product/architecture
- Related: `ADR-016-release-readiness-evidence-and-go-no-go-gate.md`, `.github/pull_request_template.md`, `docs/release-evidence.md`

## Context
ADR-016 defines the required release evidence bundle, but without repository-anchored artifacts contributors can still apply the policy inconsistently.

## Decision
1. The canonical release evidence schema is documented in `docs/release-evidence.md`.
2. `.github/pull_request_template.md` includes a matching release-evidence section so release-candidate PRs capture the same required fields.
3. `HANDOFF_REST_MCP_MVP.md` must point to `docs/release-evidence.md` and use the same headings for implementation handoffs.

## Consequences
### Positive
- Reduces interpretation drift between architecture policy and implementation workflow.
- Makes go/no-go evidence auditable from both PR and handoff artifacts.

### Tradeoffs
- Slightly longer PR descriptions for release candidates.
- Template updates require discipline when contract fields evolve.
