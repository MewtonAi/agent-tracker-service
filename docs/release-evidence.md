# Release Evidence Bundle (ADR-016 / ADR-017 / ADR-019 / ADR-020 / ADR-021 / ADR-022 / ADR-023)

Use this checklist for any PR that claims REST + MCP release readiness.

## 1) CI gate evidence (required)
- [ ] `./gradlew check` passed on **JDK 21**
- [ ] CI run URL:
- [ ] Commit SHA validated in CI:
- [ ] Evidence source declared (`local-java21` / `ci-java21`):
- [ ] Java preflight recorded (`java -version`):
- [ ] `JAVA_HOME` status recorded (set/unset):

## 2) OpenAPI snapshot evidence (required)
- [ ] `verifyOpenApiSnapshot` passed
- [ ] Snapshot diff status recorded:
  - [ ] No change in `openapi/openapi.yaml`
  - [ ] Snapshot changed intentionally and was committed in this PR
- [ ] Pagination markers confirmed in snapshot: `limit`, `cursor`, `nextCursor`

## 3) REST/MCP parity evidence (required)
- [ ] Parity tests were included via default `check` gate (no selective bypass)
- [ ] Representative parity coverage noted (for example: create/get/list/update-status, idempotency mismatch, correlation failures)

## 4) Minimum release test-signal set (required, ADR-022 + ADR-023)
- [ ] `TaskRestMcpParityTest` status recorded as `PASS` / `FAIL` / `NOT_RUN`
- [ ] `TaskMcpRuntimeTransportContractTest` status recorded as `PASS` / `FAIL` / `NOT_RUN`
- [ ] `OpenApiSnapshotContractTest` status recorded as `PASS` / `FAIL` / `NOT_RUN`
- [ ] `ReleaseReadinessDocumentationContractTest` status recorded as `PASS` / `FAIL` / `NOT_RUN`
- [ ] Signal source reference recorded (CI run URL or local Java21 log)
- [ ] Any `FAIL`/`NOT_RUN` signal keeps decision at `NO-GO`

## 5) Canonical ADR references (required)
Reference active contracts only:
- [ ] `ADR-012-mcp-correlation-id-canonicalization-policy.md`
- [ ] `ADR-013-task-list-pagination-ordering-contract.md`
- [ ] `ADR-014-contract-source-of-truth-and-supersession-policy.md`
- [ ] `ADR-015-cursor-token-evolution-and-backward-compatibility.md`
- [ ] `ADR-016-release-readiness-evidence-and-go-no-go-gate.md`
- [ ] `ADR-017-release-evidence-artifact-and-pr-template-policy.md`
- [ ] `ADR-018-release-candidate-readiness-lanes-and-feature-freeze-policy.md`
- [ ] `ADR-019-release-evidence-provenance-and-freshness-policy.md`
- [ ] `ADR-020-release-contract-documentation-coverage-policy.md`
- [ ] `ADR-021-java21-toolchain-readiness-and-verification-provenance-ladder.md`
- [ ] `ADR-022-release-evidence-minimum-test-signal-set-policy.md`
- [ ] `ADR-023-release-evidence-signal-status-normalization-and-no-go-default.md`

## 6) Environment constraints
- [ ] Local/runtime limitation recorded (or mark none)
- [ ] If constrained, CI fallback source is declared and linked
- [ ] ADR-021 provenance-ladder path is explicitly stated

## 7) Mongo index verification signal (required for mongo-backed release candidates)
- [ ] `MongoTaskStoreIntegrationTest.shouldProvisionRequiredMongoIndexesFromManifest` status recorded as `PASS` / `FAIL` / `NOT_RUN`
- [ ] Startup verification event observed (`event=mongo_index_state`) from current candidate run/log source
- [ ] `docs/mongo-index-manifest.md` reviewed and matches implementation

## 8) Handoff + decision consistency
- [ ] Handoff note includes the same evidence headings and outcomes
- [ ] Any unresolved risk/blocker is explicitly listed
- [ ] Explicit release decision captured: `GO` or `NO-GO` (owner + timestamp)
- [ ] Decision evidence is fresh (<=24h) at decision time
- [ ] If freshness exceeded, exception owner + expiry are recorded or decision remains `NO-GO`
