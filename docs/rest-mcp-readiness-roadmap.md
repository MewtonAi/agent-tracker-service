# REST + MCP Readiness Roadmap

Last updated: 2026-02-24 (PST)
Owner: Product/Architecture

## Sequencing model
- **Lane 0 (contract integrity):** OpenAPI + `check` gate green on JDK 21.
- **Lane 1 (evidence closure):** release evidence completed with provenance/freshness constraints.
- **Lane 2 (forward hardening):** cursor phase-2 and platform hardening after Lane 1.

## Sprint-ready slices

### Slice A — TKT-P1-G15 (must ship first)
- Objective: close OpenAPI drift uncertainty.
- Deliverables:
  - Java-21 execution evidence for `updateOpenApiSnapshot` + `check`
  - committed `openapi/openapi.yaml` diff (if any)
  - CI run URL + SHA recorded
- Exit criteria: `verifyOpenApiSnapshot` and full `check` pass.

### Slice B — TKT-P1-G19
- Objective: enforce ADR-019 evidence provenance/freshness.
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
