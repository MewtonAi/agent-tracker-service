# Developer Handoff — REST + MCP MVP Readiness

## What is already stable
- Task REST API exists and is test-covered.
- Transition matrix is codified in `TaskTransitionPolicy`.
- Error mapping includes conflict/not-found paths.

## Build order (recommended)

### Step 1: Introduce persistence ports/adapters without breaking controllers
- Keep `TaskLifecycleContract` as seam.
- Add Mongo-backed implementation and switch bean wiring by environment.
- Preserve response DTOs and endpoint signatures for compatibility.

### Step 2: Add idempotency + optimistic locking
- Define idempotency key contract for mutations.
- Persist dedupe record or enforce unique external mutation key.
- Add version field and compare-and-set update semantics.

### Step 3: Add MCP adapter on same application services
- Implement tools: `create_task`, `get_task`, `list_tasks`, `update_task_status`.
- Keep handlers thin: mapping only, no business logic.

### Step 4: Add parity and contract tests
- Cross-adapter scenario tests (REST vs MCP) for:
  - create
  - get
  - list with filters
  - invalid transition conflict
  - not found
- Generate and commit OpenAPI output to lock REST contract.

## Non-obvious risks
- Assignment currently allows terminal states; enforce invariant during service hardening.
- Unassign endpoint accepts nullable body; decide explicit contract (`requestedBy` required or default strategy).
- Transition policy permits same-state no-op; ensure this is intentional and documented for clients.

## Done definition for MVP
- Durable Mongo persistence with concurrency safety.
- Idempotent mutation semantics.
- MCP tools for core lifecycle.
- REST/MCP parity test suite green in CI.
