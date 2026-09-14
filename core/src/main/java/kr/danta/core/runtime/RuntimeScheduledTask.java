package kr.danta.core.runtime;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable runtime-based scheduled task definition.
 *
 * <p>Tasks store a logical type and string payload instead of a Runnable so the
 * scheduler can later be persisted/reconstructed without serialising code.</p>
 */
public record RuntimeScheduledTask(
        UUID id,
        long dueRuntimeMillis,
        String taskType,
        Map<String, String> payload
) {
    public RuntimeScheduledTask {
        Objects.requireNonNull(id, "id");
        if (dueRuntimeMillis < 0L) {
            throw new IllegalArgumentException("dueRuntimeMillis must be >= 0");
        }
        if (taskType == null || taskType.isBlank()) {
            throw new IllegalArgumentException("taskType must not be blank");
        }
        payload = payload == null ? Map.of() : Map.copyOf(payload);
    }
}
