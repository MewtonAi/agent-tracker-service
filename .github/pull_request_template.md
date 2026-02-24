## Summary
- 

## Release Evidence Bundle (required for release candidates)
Source of truth: `docs/release-evidence.md` (ADR-016 / ADR-017 / ADR-019)

### 1) CI gate evidence
- [ ] `./gradlew check` passed on JDK 21
- [ ] CI run URL:
- [ ] Commit SHA:
- [ ] Evidence source declared (CI / Java-enabled workstation)

### 2) OpenAPI snapshot evidence
- [ ] `verifyOpenApiSnapshot` passed
- [ ] `openapi/openapi.yaml` unchanged **or** intentionally updated in this PR
- [ ] If changed, rationale linked:

### 3) REST/MCP parity evidence
- [ ] Parity suite included via default `check` gate
- [ ] No selective test bypass used for release branch
- [ ] Coverage notes:

### 4) Canonical ADR evidence
- Canonical ADR set used: ADR-012/013/014/015/016/017/018/019
- [ ] Active policy references limited to canonical ADR set above
- [ ] No superseded ADR cited as active contract

### 5) Provenance + freshness checks
- [ ] Evidence SHA matches PR head SHA
- [ ] Verification evidence is <=24h old at GO decision time
- [ ] If older than 24h, verification rerun or explicit NO-GO/exception owner recorded

### 6) Environment constraints
- [ ] Local/runtime limitation documented (or mark none)
- [ ] Evidence source declared if constrained (CI or Java-enabled workstation)

### 7) Go/No-Go statement
- Decision: GO / NO-GO
- Decision owner:
- Timestamp (PST):
- Risk notes / follow-ups:
