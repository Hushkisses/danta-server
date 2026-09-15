package kr.danta.core.research;

import java.util.Objects;
import java.util.UUID;

/** One nation research item: active when taskId/dueRuntime are present, otherwise reserved in queue. */
public record ResearchQueueEntry(UUID entryId, String researchId, UUID taskId, Long dueRuntimeMillis) {
    public ResearchQueueEntry {
        Objects.requireNonNull(entryId, "entryId");
        Objects.requireNonNull(researchId, "researchId");
        researchId = researchId.trim().toLowerCase();
        if (researchId.isBlank()) throw new IllegalArgumentException("researchId is blank");
        if ((taskId == null) != (dueRuntimeMillis == null)) throw new IllegalArgumentException("taskId and dueRuntimeMillis must both be present or absent");
        if (dueRuntimeMillis != null && dueRuntimeMillis < 0) throw new IllegalArgumentException("dueRuntimeMillis < 0");
    }
    public boolean active() { return taskId != null; }
    public ResearchQueueEntry activate(UUID taskId, long dueRuntimeMillis) { return new ResearchQueueEntry(entryId, researchId, taskId, dueRuntimeMillis); }
}
