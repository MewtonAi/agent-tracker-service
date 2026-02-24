# ADR-023: Release evidence signal status normalization and NO-GO default

- Status: Accepted
- Date: 2026-02-24
- Deciders: Product/Architecture
- Supersedes: none
- Related: ADR-016, ADR-019, ADR-022

## Context
ADR-022 requires release evidence to include a minimum set of test signals, but documentation still allows ambiguous free-form status wording (for example: "looks good", "mostly green", "pending rerun"). That ambiguity weakens GO/NO-GO auditability and makes cross-review inconsistent.

We need a normalized status vocabulary and deterministic blocking rule so release decisions are machine-reviewable and human-consistent.

## Decision
For each required release signal (`TaskRestMcpParityTest`, `TaskMcpRuntimeTransportContractTest`, `OpenApiSnapshotContractTest`, `ReleaseReadinessDocumentationContractTest`), release evidence artifacts MUST record:

1. **Status** using only: `PASS`, `FAIL`, or `NOT_RUN`
2. **Evidence source** (CI URL or local Java21 log reference)
3. **Evidence SHA parity** (matches PR head SHA)

Decision rule:
- Any required signal with `FAIL` or `NOT_RUN` => release decision remains **NO-GO**.
- `GO` is permitted only when all required signals are `PASS`, with freshness/provenance constraints from ADR-019 and preflight/source constraints from ADR-021 satisfied.

## Consequences
### Positive
- Reduces interpretation drift in release reviews.
- Makes policy checks straightforward for future automation.
- Tightens handoff quality by forcing explicit unresolved-state signaling.

### Trade-offs
- Slightly more template verbosity.
- Teams must avoid informal status text in release-critical sections.

## Implementation notes
- Update `docs/release-evidence.md` and `.github/pull_request_template.md` to include a normalized signal table/checklist.
- Update planning/handoff docs so ticket language references normalized statuses and NO-GO default.
- Extend `ReleaseReadinessDocumentationContractTest` to require ADR-023 references and status vocabulary markers.

## Verification
- Documentation contract tests enforce ADR-023 references and required status markers.
- PR reviewers reject release claims with missing/ambiguous signal status values.
