# Developer Handoff — REST + MCP Readiness

Last updated: 2026-02-24 (PST, ADR-021 Java21 provenance ladder + planning refresh)

## What was accomplished in this pass
- Re-inspected current docs/test/CI posture for REST+MCP release readiness.
- Added **ADR-021** (`ADR-021-java21-toolchain-readiness-and-verification-provenance-ladder.md`) to formalize Java 21 preflight + verification-source policy.
- Updated planning and release evidence artifacts to require explicit preflight declaration:
  - `java -version`
  - `JAVA_HOME` set/unset state
  - evidence source label (`local-java21` or `ci-java21`)
- Re-prioritized backlog and roadmap to run toolchain-preflight ticket before OpenAPI reconciliation.

## Current state to assume
1. **CI gate:** `.github/workflows/ci.yml` runs JDK 21 + `./gradlew check`.
2. **Primary blocker:** local shell lacks Java runtime (`JAVA_HOME`/`java` missing), so verification must be CI-backed unless local Java 21 is provisioned.
3. **Release lane policy:** ADR-018 feature-freeze still applies until lane closure.
4. **Evidence policy:** ADR-019 freshness/SHA parity + ADR-021 source/preflight declaration are both required.
5. **Docs governance policy:** ADR-020 still requires same-PR doc/test sync for release-policy ADR additions.

## Next coding slice (strict order)
1. **TKT-P1-G21** — capture Java preflight + declare verification source.
2. **TKT-P1-G15** — run Java-21 verification (`updateOpenApiSnapshot`, `check`) and commit snapshot only if regenerated.
3. **TKT-P1-G19** — complete release evidence + PR template provenance/freshness/preflight fields.
4. **TKT-P1-G17** — final canonical ADR reference hygiene sweep.
5. **TKT-P2-A18** — continue cursor phase-2 planning after lane closure.

## Done criteria for immediate slice
- CI green for PR head SHA.
- `verifyOpenApiSnapshot` pass outcome captured.
- `docs/release-evidence.md` populated with source + freshness + preflight evidence.
- GO/NO-GO statement includes owner/timestamp and <=24h freshness at decision time.

## Canonical ADR set for active release work
- `ADR-012-mcp-correlation-id-canonicalization-policy.md`
- `ADR-013-task-list-pagination-ordering-contract.md`
- `ADR-014-contract-source-of-truth-and-supersession-policy.md`
- `ADR-015-cursor-token-evolution-and-backward-compatibility.md`
- `ADR-016-release-readiness-evidence-and-go-no-go-gate.md`
- `ADR-017-release-evidence-artifact-and-pr-template-policy.md`
- `ADR-018-release-candidate-readiness-lanes-and-feature-freeze-policy.md`
- `ADR-019-release-evidence-provenance-and-freshness-policy.md`
- `ADR-020-release-contract-documentation-coverage-policy.md`
- `ADR-021-java21-toolchain-readiness-and-verification-provenance-ladder.md`

## Environment caveat for this run
- Local verification was blocked in-session (`JAVA_HOME` unset, `java` missing), so no Java-dependent checks were executed from this shell.
