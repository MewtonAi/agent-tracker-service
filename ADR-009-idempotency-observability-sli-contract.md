# ADR-009: Idempotency Observability SLI Contract (Post-MVP)

- **Status:** Accepted
- **Date:** 2026-02-24
- **Deciders:** product/architecture
- **Related:** `ADR-007-idempotency-v2-only-posture.md`, `PRODUCT_OWNER_NEXT.md`, `ARCHITECTURE.md`

## Context
The service emits structured idempotency markers (`idempotency.first_write`, `idempotency.replay_hit`, `idempotency.mismatch_reject`) but currently relies on logs for operational visibility.

For sustained production readiness, operators need stable, queryable SLIs for replay behavior and mismatch anomalies.

## Decision
1. Define idempotency observability as a versioned contract with three required counters:
   - `idempotency_first_write_total{operation}`
   - `idempotency_replay_hit_total{operation}`
   - `idempotency_mismatch_reject_total{operation}`
2. Define two derived SLIs:
   - **Replay ratio** = replay hits / (first writes + replay hits)
   - **Mismatch rate** = mismatch rejects / (first writes + replay hits + mismatch rejects)
3. Keep `operation` as the mandatory dimension to preserve debugging value and parity comparisons.
4. Treat changes to metric names or formulas as contract changes requiring ADR update.

## Why
- Converts log breadcrumbs into durable operational signals.
- Establishes shared vocabulary for alerts, dashboards, and incident response.
- Prevents accidental observability drift when refactoring command flows.

## Consequences
### Positive
- Faster anomaly detection for replay/mismatch spikes.
- More reliable capacity/risk conversations using stable ratios.

### Tradeoffs
- Slightly higher implementation overhead and test burden.
- Requires dashboard/alert ownership discipline.

## Guardrails
- Counter emission must be covered in automated tests for both create and update-status flows.
- Dashboard and threshold defaults must be documented in repo alongside implementation.
- If additional dimensions are added, `operation` remains mandatory and high-cardinality dimensions must be justified.
