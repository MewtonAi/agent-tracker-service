# Cursor Phase-2 Readiness (TKT-P2-A18)

Owner: Platform API
Status: Draft-ready (post Lane-1 freeze)

## Scope guardrails
- External contract fields remain unchanged: `cursor`, `nextCursor`, `limit`.
- v1 continues to emit offset `nextCursor` values (`<n>`).
- Decoder accepts `<n>` and `o:<n>`; seek-family (`s:<token>`) remains rejected until phase-2 activation.

## Mixed-token parity matrix (REST + MCP)
| Scenario | Request cursor | Expected behavior |
|---|---|---|
| First page | _(absent)_ | start at offset 0 |
| Legacy offset | `2` | accepted, returns page from offset 2 |
| Prefixed offset | `o:2` / `O: 2` | accepted, same result as `2` |
| Terminal page | `cursor` beyond dataset | empty `tasks`, `nextCursor = null` |
| Malformed offset | `o:` / `o:abc` / `o:-1` | BAD_REQUEST |
| Reserved seek token | `s:...` | BAD_REQUEST (not enabled yet) |
| Unknown family | `x:1` | BAD_REQUEST |

## Rollout + rollback
- **Activation switch (future):** `pagination.cursor.seek.enabled` (default `false`).
- **Rollback owner:** Platform API on-call.
- **Rollback action:** set `pagination.cursor.seek.enabled=false` to force offset-only decode/emit behavior.
- **Invariant:** no OpenAPI field-shape changes during toggle.
