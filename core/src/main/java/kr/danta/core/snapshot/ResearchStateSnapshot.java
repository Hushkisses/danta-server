package kr.danta.core.snapshot;

import kr.danta.core.research.DoctrineSelection;
import java.util.List;
import java.util.Set;

public record ResearchStateSnapshot(String nationId, int researchSlots, Set<String> completed, List<ResearchQueueSnapshot> queue, int doctrineSlots, List<DoctrineSelection> doctrines) {
    public ResearchStateSnapshot {
        completed = completed == null ? Set.of() : Set.copyOf(completed);
        queue = queue == null ? List.of() : List.copyOf(queue);
        doctrines = doctrines == null ? List.of() : List.copyOf(doctrines);
    }
    /** Snapshot v14 compatibility: doctrine state did not exist and defaults to the design baseline of two empty slots. */
    public ResearchStateSnapshot(String nationId, int researchSlots, Set<String> completed, List<ResearchQueueSnapshot> queue) {
        this(nationId, researchSlots, completed, queue, 2, List.of());
    }
}
