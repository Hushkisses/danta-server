package kr.danta.core.snapshot;

import java.util.List;

/** Persisted DEV-034 remaining sequential destinations for an army. */
public record ArmyOperationQueueSnapshot(String armyId, List<String> destinations) {
    public ArmyOperationQueueSnapshot {
        destinations = destinations == null ? List.of() : List.copyOf(destinations);
    }
}
