package kr.danta.core.snapshot;

import java.util.List;
import java.util.Set;

public record ResearchStateSnapshot(String nationId, int researchSlots, Set<String> completed, List<ResearchQueueSnapshot> queue) {
    public ResearchStateSnapshot {
        completed = completed == null ? Set.of() : Set.copyOf(completed);
        queue = queue == null ? List.of() : List.copyOf(queue);
    }
}
