package agent.tracker.service.application;

import agent.tracker.service.domain.exception.NotFoundException;
import agent.tracker.service.domain.model.Task;
import agent.tracker.service.domain.model.TaskStatus;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class TaskQueryService {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 200;

    private final TaskStore store;

    public TaskQueryService(TaskStore store) {
        this.store = store;
    }

    public Task getTaskById(String taskId) {
        Task task = store.findTaskById(taskId);
        if (task == null) {
            throw new NotFoundException("Task not found: " + taskId);
        }
        return task;
    }

    public List<Task> listTasks(TaskStatus status) {
        return listTasks(status, null, DEFAULT_LIMIT).tasks();
    }

    public TaskListPage listTasks(TaskStatus status, String cursor, Integer limit) {
        int resolvedLimit = resolveLimit(limit);
        int offset = decodeCursor(cursor);

        TaskStorePage page = store.listTasksPage(status, offset, resolvedLimit);
        String nextCursor = page.hasMore() ? String.valueOf(offset + page.tasks().size()) : null;
        return new TaskListPage(page.tasks(), nextCursor);
    }

    private static int resolveLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException("limit must be between 1 and " + MAX_LIMIT);
        }
        return limit;
    }

    private static int decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return 0;
        }

        String normalized = cursor.trim();
        if (normalized.regionMatches(true, 0, "o:", 0, 2)) {
            normalized = normalized.substring(2);
        }

        try {
            int value = Integer.parseInt(normalized);
            if (value < 0) {
                throw new IllegalArgumentException("cursor must be a non-negative integer or offset token (o:<n>)");
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("cursor must be a non-negative integer or offset token (o:<n>)");
        }
    }
}
