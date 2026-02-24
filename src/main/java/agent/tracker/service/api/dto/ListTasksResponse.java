package agent.tracker.service.api.dto;

import java.util.List;

public record ListTasksResponse(List<TaskResponse> tasks, String nextCursor) {
}
