# ADR-021: Java 21 Toolchain Readiness and Verification Provenance Ladder

- **Status:** Accepted
- **Date:** 2026-02-24
- **Owners:** Product/Architecture

## Context
Release-readiness execution repeatedly depends on Java 21 for `./gradlew check`, `verifyOpenApiSnapshot`, and parity contracts. In constrained shells, local verification can be blocked (`JAVA_HOME` unset, `java` missing), which delays lane closure and weakens confidence unless fallback evidence is explicit and auditable.

ADR-019 already defines provenance and freshness expectations. We need a deterministic preflight policy that clarifies *where* verification is allowed and how teams must escalate when local execution is unavailable.

## Decision
Adopt a **verification provenance ladder** for release-critical checks:

1. **Preferred:** Local Java 21 workstation verification (developer machine) with explicit preflight evidence.
2. **Fallback:** CI-only verification when local Java 21 is unavailable.
3. **Exception path:** No GO decision unless evidence includes (a) source declaration, (b) PR-head SHA parity, and (c) freshness <=24h (ADR-019).

For any PR claiming release readiness, evidence must include a Java preflight declaration:
- `java -version` result (or explicit absence)
- `JAVA_HOME` presence/absence statement
- Verification source (`local-java21` or `ci-java21`)

## Consequences
### Positive
- Reduces ambiguity when local shells are constrained.
- Keeps release decisions deterministic even in heterogeneous environments.
- Improves handoff clarity for the next coding slice.

### Trade-offs
- Adds small documentation overhead in PR/release evidence templates.
- Requires doc/test updates whenever provenance wording evolves.

## Guardrails
- This ADR extends ADR-019; it does not relax freshness or SHA-parity requirements.
- No release GO claim is valid with missing Java-preflight declaration.
- Documentation contract tests must enforce presence of provenance-ladder references in release evidence artifacts.

## Related
- ADR-016 (GO/NO-GO evidence gate)
- ADR-017 (artifact/template policy)
- ADR-018 (lane sequencing and freeze posture)
- ADR-019 (provenance/freshness)
- ADR-020 (documentation contract coverage policy)
