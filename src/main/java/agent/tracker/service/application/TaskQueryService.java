package agent.tracker.service.application;

import agent.tracker.service.application.contract.TaskInputNormalizer;
import agent.tracker.service.domain.exception.NotFoundException;
import agent.tracker.service.domain.model.Task;
import agent.tracker.service.domain.model.TaskStatus;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class TaskQueryService {

    private final TaskStore store;
    private final TaskInputNormalizer inputNormalizer;

    public TaskQueryService(TaskStore store, TaskInputNormalizer inputNormalizer) {
        this.store = store;
        this.inputNormalizer = inputNormalizer;
    }

    TaskQueryService(TaskStore store) {
        this(store, new TaskInputNormalizer());
    }

    public Task getTaskById(String taskId) {
        Task task = store.findTaskById(taskId);
        if (task == null) {
            throw new NotFoundException("Task not found: " + taskId);
        }
        return task;
    }

    public List<Task> listTasks(TaskStatus status) {
        return listTasks(status, null, TaskInputNormalizer.DEFAULT_LIMIT).tasks();
    }

    public TaskListPage listTasks(TaskStatus status, String cursor, Integer limit) {
        int resolvedLimit = inputNormalizer.normalizeLimit(limit);
        int offset = TaskPaginationCursorCodec.decodeOffset(cursor);

        TaskStorePage page = store.listTasksPage(status, offset, resolvedLimit);
        String nextCursor = page.hasMore() ? TaskPaginationCursorCodec.encodeNextCursor(offset, page.tasks().size()) : null;
        return new TaskListPage(page.tasks(), nextCursor);
    }
}
