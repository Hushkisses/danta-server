package kr.danta.core.snapshot;

import kr.danta.core.general.GeneralGrade;
import kr.danta.core.general.GeneralHealthStatus;

import java.util.List;

/** DEV-076F persisted owned-general season state. */
public record GeneralSnapshot(
        String generalId, String ownerNationId, GeneralGrade grade, int level,
        int command, int martial, int strategy, int logistics,
        List<String> traitIds, List<String> abilityIds,
        String commandedArmyId, String assignedPointId,
        GeneralHealthStatus healthStatus, Long injuredAtRuntimeMillis, Long recoveryReadyAtRuntimeMillis,
        String captorNationId, Long capturedAtRuntimeMillis, Long detentionEndsAtRuntimeMillis
) {
    public GeneralSnapshot {
        traitIds = traitIds == null ? List.of() : List.copyOf(traitIds);
        abilityIds = abilityIds == null ? List.of() : List.copyOf(abilityIds);
    }
}
