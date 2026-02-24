# ADR-020: Release-contract documentation coverage policy

- Status: Accepted
- Date: 2026-02-24
- Deciders: product/architecture
- Related: `ADR-014-contract-source-of-truth-and-supersession-policy.md`, `ADR-016-release-readiness-evidence-and-go-no-go-gate.md`, `ADR-017-release-evidence-artifact-and-pr-template-policy.md`, `ADR-018-release-candidate-readiness-lanes-and-feature-freeze-policy.md`, `ADR-019-release-evidence-provenance-and-freshness-policy.md`, `src/test/java/agent/tracker/service/docs/ReleaseReadinessDocumentationContractTest.java`

## Context
Release-readiness policy now spans lane sequencing, evidence artifacts, and provenance/freshness checks. The existing documentation contract test originally enforced ADR-016/017-era references only, which creates drift risk:
- New canonical release ADRs (018/019) can be omitted from active docs without test failure.
- Planning and handoff artifacts can silently diverge from canonical release policy.
- Teams may treat superseded or partial references as release-ready guidance.

## Decision
1. Documentation contract tests must validate the full canonical release ADR set: **ADR-012/013/014/015/016/017/018/019/020**.
2. Release-planning artifacts (`PRODUCT_OWNER_NEXT.md`, `docs/rest-mcp-readiness-roadmap.md`) are part of the contract surface and must remain aligned.
3. Release evidence + PR template checks must explicitly include ADR-019 provenance/freshness obligations.
4. Any future release-policy ADR addition requires same-PR updates to docs contract tests.

## Consequences
### Positive
- Canonical release policy remains machine-checked across architecture, handoff, and planning artifacts.
- Reduces regression risk where docs pass review but violate active release governance.
- Makes future release-policy evolution explicit and test-gated.

### Tradeoffs
- Slightly higher maintenance burden when adding release-policy ADRs.
- Planning docs become stricter and may require synchronized edits in policy PRs.
