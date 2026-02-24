# ADR-022: Release Evidence Minimum Test-Signal Set Policy

- **Status:** Accepted
- **Date:** 2026-02-24
- **Owners:** Product/Architecture

## Context
Release-readiness currently depends on a generic `./gradlew check` signal plus provenance/freshness evidence. Reviewers still have to infer whether key parity/runtime/docs-contract checks were included in the run used for GO/NO-GO.

## Decision
Release claims must declare a **minimum test-signal set** with explicit status and source reference:

1. `TaskRestMcpParityTest`
2. `TaskMcpRuntimeTransportContractTest`
3. `OpenApiSnapshotContractTest`
4. `ReleaseReadinessDocumentationContractTest`

Required evidence fields:
- pass/fail status per signal
- source reference (CI URL or local Java21 log)
- SHA parity with PR head (ADR-019/021 remain mandatory)

## Consequences
- Stronger release-review confidence through compact, auditable signals.
- Slight documentation overhead in PR/release evidence artifacts.
- Signal-set changes require synchronized docs + contract-test updates.

## Guardrails
- Green `check` without declared signal statuses is insufficient for GO.
- Missing signal status defaults to `NO-GO` until resolved.
- This ADR extends ADR-016/019/021; it does not weaken them.

## Related
- ADR-016 (GO/NO-GO gate)
- ADR-017 (artifact/template policy)
- ADR-018 (lane sequencing)
- ADR-019 (provenance/freshness)
- ADR-020 (documentation contract coverage)
- ADR-021 (Java21 provenance ladder)
