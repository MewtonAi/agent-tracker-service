## Summary
- 

## Release Evidence Bundle (required for release candidates)
Source of truth: `docs/release-evidence.md` (ADR-016 / ADR-017 / ADR-019 / ADR-020 / ADR-021 / ADR-022 / ADR-023)

### 1) CI gate evidence
- [ ] `./gradlew check` passed on JDK 21
- [ ] CI run URL:
- [ ] Commit SHA:
- [ ] Evidence source declared (`local-java21` / `ci-java21`)
- [ ] Java preflight (`java -version`) captured
- [ ] `JAVA_HOME` status captured

### 2) OpenAPI snapshot evidence
- [ ] `verifyOpenApiSnapshot` passed
- [ ] `openapi/openapi.yaml` unchanged **or** intentionally updated in this PR
- [ ] Pagination markers confirmed (`limit`, `cursor`, `nextCursor`)
- [ ] If changed, rationale linked:

### 3) REST/MCP parity evidence
- [ ] Parity suite included via default `check` gate
- [ ] No selective test bypass used for release branch
- [ ] Coverage notes:

### 4) Minimum release test-signal set (ADR-022 / ADR-023)
- [ ] `TaskRestMcpParityTest` status recorded as `PASS` / `FAIL` / `NOT_RUN`
- [ ] `TaskMcpRuntimeTransportContractTest` status recorded as `PASS` / `FAIL` / `NOT_RUN`
- [ ] `OpenApiSnapshotContractTest` status recorded as `PASS` / `FAIL` / `NOT_RUN`
- [ ] `ReleaseReadinessDocumentationContractTest` status recorded as `PASS` / `FAIL` / `NOT_RUN`
- [ ] Signal source reference (CI URL or local Java21 log):
- [ ] Any `FAIL`/`NOT_RUN` signal keeps decision at `NO-GO`

### 5) Canonical ADR evidence
- Canonical ADR set used: ADR-012/013/014/015/016/017/018/019/020/021/022/023
- [ ] Active policy references limited to canonical ADR set above
- [ ] No superseded ADR cited as active contract

### 6) Provenance + freshness checks
- [ ] Evidence SHA matches PR head SHA
- [ ] Verification evidence is <=24h old at GO decision time
- [ ] If older than 24h, verification rerun or explicit NO-GO/exception owner recorded

### 7) Environment constraints
- [ ] Local/runtime limitation documented (or mark none)
- [ ] Evidence source declared if constrained (CI or Java-enabled workstation)

### 8) Go/No-Go statement
- Decision: GO / NO-GO
- Decision owner:
- Timestamp (PST):
- Risk notes / follow-ups:
