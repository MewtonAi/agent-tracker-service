package agent.tracker.service.application.contract;

import agent.tracker.service.domain.model.TaskStatus;
import jakarta.inject.Singleton;
import java.util.Arrays;
import java.util.Locale;

@Singleton
public class TaskInputNormalizer {

    public static final int DEFAULT_LIMIT = 50;
    public static final int MAX_LIMIT = 200;

    public TaskStatus parseStatusFilter(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return TaskStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            String allowed = Arrays.stream(TaskStatus.values())
                .map(Enum::name)
                .sorted()
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
            throw new IllegalArgumentException("status must be one of [" + allowed + "]");
        }
    }

    public String normalizeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        return cursor.trim();
    }

    public int normalizeLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException("limit must be between 1 and " + MAX_LIMIT);
        }
        return limit;
    }

    public String requireIdempotencyKey(String idempotencyKey) {
        return requireText(idempotencyKey, "idempotencyKey");
    }

    public String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }
}
