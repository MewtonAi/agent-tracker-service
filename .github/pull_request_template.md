## Summary
- 

## Release Evidence Bundle (required for release candidates)
Source of truth: `docs/release-evidence.md` (ADR-016 / ADR-017)

### 1) CI gate evidence
- [ ] `./gradlew check` passed on JDK 21
- [ ] CI run URL:
- [ ] Commit SHA:

### 2) OpenAPI snapshot evidence
- [ ] `verifyOpenApiSnapshot` passed
- [ ] `openapi/openapi.yaml` unchanged **or** intentionally updated in this PR
- [ ] If changed, rationale linked:

### 3) REST/MCP parity evidence
- [ ] Parity suite included via default `check` gate
- [ ] No selective test bypass used for release branch
- [ ] Coverage notes:

### 4) Canonical ADR evidence
- [ ] Active policy references limited to ADR-012/013/014/015/016/017
- [ ] No superseded ADR cited as active contract

### 5) Environment constraints
- [ ] Local/runtime limitation documented (or mark none)
- [ ] Evidence source declared if constrained (CI or Java-enabled workstation)

### 6) Go/No-Go statement
- Decision: GO / NO-GO
- Decision owner:
- Timestamp (PST):
- Risk notes / follow-ups:
