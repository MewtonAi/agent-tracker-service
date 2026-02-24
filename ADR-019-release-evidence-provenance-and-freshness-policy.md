# ADR-019: Release-evidence provenance and freshness policy

- Status: Accepted
- Date: 2026-02-24
- Deciders: product/architecture
- Related: `ADR-016-release-readiness-evidence-and-go-no-go-gate.md`, `ADR-017-release-evidence-artifact-and-pr-template-policy.md`, `ADR-018-release-candidate-readiness-lanes-and-feature-freeze-policy.md`, `docs/release-evidence.md`, `.github/pull_request_template.md`

## Context
Release confidence depends on objective evidence, but local Java/runtime constraints can force teams to rely on external CI or another workstation. Without a provenance rule:
- Evidence may not correspond to the exact PR head commit.
- Evidence can become stale before a GO decision is made.
- Handoff quality degrades when source and timestamp are omitted.

## Decision
For any release-candidate GO/NO-GO claim:
1. **Evidence source must be explicit** (CI run or named Java-enabled workstation).
2. **Commit SHA parity is mandatory** (evidence SHA equals PR head SHA).
3. **Freshness window is <=24 hours** between successful verification and decision timestamp.
4. If freshness is exceeded, rerun verification or mark decision `NO-GO` until refreshed.
5. Any exception requires named owner + justification + explicit expiry timestamp.

## Consequences
### Positive
- GO decisions become auditable and reproducible.
- Reduces false confidence from stale or mismatched evidence.
- Clarifies expectations when local runtime limitations exist.

### Tradeoffs
- Adds process overhead to release-candidate PR completion.
- May require re-running CI for long-lived PRs near merge time.
