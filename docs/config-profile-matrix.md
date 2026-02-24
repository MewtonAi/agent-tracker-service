# Micronaut Configuration Profile Matrix

| Profile | Intended use | `task.store` default | Mongo contract | `idempotency.ttl-hours` default | Management endpoint exposure | MCP transport default |
|---|---|---|---|---|---|---|
| `local` | Developer workstation | `memory` (`TASK_STORE` override allowed) | Local URI/database defaults with env override | `48` | Inherits base `application.yml` (all enabled, non-sensitive) | `HTTP` |
| `test` | Deterministic automated tests | `memory` | Fixed test URI/database values (unused unless explicitly switched to mongo) | `1` | Disabled (`micronaut.server.endpoints.all.enabled=false`) | `HTTP` |
| `prod` | Production/runtime environments | `mongo` | `MONGODB_URI` required, database override optional | `48` (env override supported) | Restricted (`enabled=false`, `sensitive=true`) | `HTTP` (`MCP_TRANSPORT` override allowed) |

## Activation examples

```bash
# local
MICRONAUT_ENVIRONMENTS=local ./gradlew run

# test (CI/local deterministic checks)
MICRONAUT_ENVIRONMENTS=test ./gradlew check

# prod
MICRONAUT_ENVIRONMENTS=prod java -jar build/libs/agent-tracker-service-*.jar
```
