# REST + MCP Readiness Roadmap

Last updated: 2026-02-24 (PST, ADR-021 Java21 provenance-ladder pass)
Owner: Product/Architecture

## Sequencing model
- **Lane 0 (contract integrity):** OpenAPI + `check` gate green on JDK 21.
- **Lane 1 (evidence closure):** release evidence completed with provenance/freshness constraints.
- **Lane 2 (forward hardening):** cursor phase-2 and platform hardening after Lane 1.

## Governing ADRs for release lanes
- Lane sequencing / temporary freeze posture: **ADR-018**
- Evidence provenance + freshness: **ADR-019**
- Documentation contract coverage + policy-sync rule: **ADR-020**
- Java21 preflight + verification provenance ladder: **ADR-021**
- Minimum release test-signal declaration set: **ADR-022**

## Sprint-ready slices

### Slice A0 — TKT-P1-G21 (must ship first)
- Objective: eliminate Java runtime ambiguity for release-lane verification.
- Deliverables:
  - Java preflight declaration (`java -version`, `JAVA_HOME`, evidence source)
  - explicit fallback to CI when local Java 21 is unavailable
  - release artifacts updated with source labels (`local-java21` / `ci-java21`)
- Exit criteria: no GO/NO-GO claim can be made without preflight declaration.

### Slice A — TKT-P1-G15
- Objective: close OpenAPI drift uncertainty.
- Deliverables:
  - Java-21 execution evidence for `updateOpenApiSnapshot` + `check`
  - committed `openapi/openapi.yaml` diff (if any)
  - CI run URL + SHA recorded
- Exit criteria: `verifyOpenApiSnapshot` and full `check` pass.

### Slice B — TKT-P1-G19
- Objective: enforce ADR-019/ADR-021 evidence provenance, freshness, and toolchain-source declaration.
- Deliverables:
  - release evidence checklist completed
  - PR template fields fully populated
  - GO/NO-GO timestamp auditable and <=24h from verification
- Exit criteria: no release claim without SHA parity + freshness proof.

### Slice C — TKT-P1-G17
- Objective: final policy-reference hygiene.
- Deliverables:
  - no active docs referencing superseded ADR variants
  - docs point to canonical ADR set consistently
  - ADR-020 documentation contract tests remain green with planning artifacts in scope (through ADR-021)
  - docs contract tests stay aligned with any release-policy ADR additions
- Exit criteria: docs contract test and manual doc review agree.

### Slice D — TKT-P2-A18 (post-freeze)
- Objective: phase-2 cursor evolution readiness.
- Deliverables:
  - updated mixed-token parity matrix
  - rollback owner + switch documented
  - no external contract field changes
- Exit criteria: plan supports seek token rollout with safe rollback.

## Risks to monitor
- Missing local Java runtime causes verification lag.
- CI evidence can become stale if PR waits too long before merge.
- Scope creep before Lane 1 closure violates ADR-018 feature-freeze intent.
- Release-policy ADR drift if docs/tests are not updated together (ADR-020).
