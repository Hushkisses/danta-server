package kr.danta.core.snapshot;

import java.util.UUID;

public record ResearchQueueSnapshot(UUID entryId, String researchId, UUID taskId, Long dueRuntimeMillis) {}
