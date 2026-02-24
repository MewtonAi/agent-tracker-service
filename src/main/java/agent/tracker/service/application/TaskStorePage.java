package agent.tracker.service.application;

import agent.tracker.service.domain.model.Task;
import java.util.List;

public record TaskStorePage(List<Task> tasks, boolean hasMore) {
}
