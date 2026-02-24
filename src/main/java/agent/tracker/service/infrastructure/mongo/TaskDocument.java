package agent.tracker.service.infrastructure.mongo;

import agent.tracker.service.domain.model.TaskPriority;
import agent.tracker.service.domain.model.TaskStatus;
import agent.tracker.service.domain.model.TaskType;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Version;
import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;

@Serdeable
@MappedEntity("tasks")
public record TaskDocument(
    @Id String taskId,
    @Version Long version,
    String title,
    @Nullable String description,
    TaskType taskType,
    TaskStatus status,
    TaskPriority priority,
    @Nullable AgentRefEmbeddable assignee,
    Instant createdAt,
    String createdBy,
    Instant updatedAt,
    String updatedBy
) {
}
