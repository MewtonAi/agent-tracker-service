package agent.tracker.service.domain.policy;

import agent.tracker.service.domain.exception.InvalidTaskTransitionException;
import agent.tracker.service.domain.model.TaskStatus;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class TaskTransitionPolicy {
    private static final Map<TaskStatus, Set<TaskStatus>> ALLOWED = new EnumMap<>(TaskStatus.class);

    static {
        ALLOWED.put(TaskStatus.BACKLOG, EnumSet.of(TaskStatus.READY, TaskStatus.CANCELLED));
        ALLOWED.put(TaskStatus.READY, EnumSet.of(TaskStatus.BACKLOG, TaskStatus.IN_PROGRESS, TaskStatus.CANCELLED));
        ALLOWED.put(TaskStatus.IN_PROGRESS, EnumSet.of(TaskStatus.READY, TaskStatus.BLOCKED, TaskStatus.IN_REVIEW, TaskStatus.CANCELLED));
        ALLOWED.put(TaskStatus.BLOCKED, EnumSet.of(TaskStatus.IN_PROGRESS, TaskStatus.CANCELLED));
        ALLOWED.put(TaskStatus.IN_REVIEW, EnumSet.of(TaskStatus.IN_PROGRESS, TaskStatus.DONE, TaskStatus.CANCELLED));
        ALLOWED.put(TaskStatus.DONE, EnumSet.noneOf(TaskStatus.class));
        ALLOWED.put(TaskStatus.CANCELLED, EnumSet.noneOf(TaskStatus.class));
    }

    private TaskTransitionPolicy() {
    }

    public static void assertTransition(TaskStatus from, TaskStatus to) {
        if (from == to) {
            return;
        }
        Set<TaskStatus> allowedTargets = ALLOWED.getOrDefault(from, Set.of());
        if (!allowedTargets.contains(to)) {
            throw new InvalidTaskTransitionException("Cannot transition task status from " + from + " to " + to);
        }
    }
}
