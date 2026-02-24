# ADR-014: Contract source-of-truth and supersession policy

- Status: Accepted
- Date: 2026-02-24
- Deciders: product/architecture
- Related: ADR-012, ADR-013, ARCHITECTURE.md, PRODUCT_OWNER_NEXT.md

## Context
The repository currently contains duplicate ADR numbers (`ADR-012`, `ADR-013`) created during rapid contract iteration. This introduces ambiguity for developers, reviewers, and future automation that relies on a single canonical policy per topic.

## Decision
1. Each contract topic must have exactly one canonical ADR reference for active implementation and testing.
2. When duplicate or interim ADRs exist, they must be explicitly marked **Superseded** and point to the canonical successor.
3. Canonical references for current work are:
   - MCP correlation policy: `ADR-012-mcp-correlation-id-canonicalization-policy.md`
   - Pagination ordering/policy: `ADR-013-task-list-pagination-ordering-contract.md`
4. Planning docs (`ARCHITECTURE.md`, `PRODUCT_OWNER_NEXT.md`, handoff notes) must reference canonical ADR filenames, not only ADR numbers.
5. Follow-up cleanup (optional, non-breaking): rename superseded duplicates to include `.superseded` marker in filename once safe for links.

## Consequences
- Reduces contract interpretation drift during implementation.
- Makes onboarding and code review faster by removing "which ADR is active?" ambiguity.
- Keeps history intact while creating a deterministic source of truth for roadmap tickets and acceptance criteria.
