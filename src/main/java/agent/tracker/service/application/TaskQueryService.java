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

        List<Task> tasks = store.listTasks(status);
        if (offset >= tasks.size()) {
            return new TaskListPage(List.of(), null);
        }

        int toIndex = Math.min(offset + resolvedLimit, tasks.size());
        List<Task> page = tasks.subList(offset, toIndex);
        String nextCursor = toIndex < tasks.size() ? String.valueOf(toIndex) : null;
        return new TaskListPage(page, nextCursor);
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
        try {
            int value = Integer.parseInt(cursor.trim());
            if (value < 0) {
                throw new IllegalArgumentException("cursor must be a non-negative integer");
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("cursor must be a non-negative integer");
        }
    }
}
