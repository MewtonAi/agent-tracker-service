package agent.tracker.service.application;

import agent.tracker.service.domain.model.Task;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class InMemoryTaskStore {
    private final Map<String, Task> tasks = new ConcurrentHashMap<>();
    private final Map<String, Task> createByIdempotency = new ConcurrentHashMap<>();
    private final Map<String, Task> statusByIdempotency = new ConcurrentHashMap<>();

    public Map<String, Task> tasks() {
        return tasks;
    }

    public Map<String, Task> createByIdempotency() {
        return createByIdempotency;
    }

    public Map<String, Task> statusByIdempotency() {
        return statusByIdempotency;
    }
}
