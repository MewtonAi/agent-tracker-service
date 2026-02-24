# OBSERVABILITY.md

## Idempotency metrics

Metric exported:
- `agent_tracker_idempotency_events_total{event,operation}`

Tags:
- `event`: `first_write`, `replay_hit`, `mismatch_reject`
- `operation`: `create_task`, `update_task_status`

Semantics:
- `first_write`: first successful processing for a given idempotency scope
- `replay_hit`: duplicate request with same key+payload replayed safely
- `mismatch_reject`: duplicate key with different payload rejected

## Dashboard formulas

Recommended rates over 5m windows:
- `replay_ratio = sum(rate(agent_tracker_idempotency_events_total{event="replay_hit"}[5m])) / clamp_min(sum(rate(agent_tracker_idempotency_events_total{event="first_write"}[5m])) + sum(rate(agent_tracker_idempotency_events_total{event="replay_hit"}[5m])), 1e-9)`
- `mismatch_rate = sum(rate(agent_tracker_idempotency_events_total{event="mismatch_reject"}[5m]))`

Also chart by operation:
- `sum by (operation,event) (rate(agent_tracker_idempotency_events_total[5m]))`

## Initial alert thresholds (post-MVP defaults)

- **Warning:** replay ratio > 0.30 for 15m
- **Critical:** replay ratio > 0.50 for 10m
- **Warning:** mismatch rate > 0.05/sec for 10m
- **Critical:** mismatch rate > 0.20/sec for 5m
- **Critical:** mismatch count >= 20 in 5m for a single operation

These are seed thresholds and should be tuned after first production baseline week.
