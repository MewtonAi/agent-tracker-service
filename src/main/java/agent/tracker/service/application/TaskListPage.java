package agent.tracker.service.application;

import agent.tracker.service.domain.model.Task;
import java.util.List;

public record TaskListPage(List<Task> tasks, String nextCursor) {
}
