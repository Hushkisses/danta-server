package kr.danta.core.snapshot;

import kr.danta.core.territory.BattlefieldTag;

import java.util.Set;

/** DEV-022 persistence representation of a strategic edge. */
public record StrategicEdgeSnapshot(
        String edgeId,
        String pointAId,
        String pointBId,
        long baseTravelMillis,
        Set<BattlefieldTag> battlefieldTags
) {
    public StrategicEdgeSnapshot {
        battlefieldTags = battlefieldTags == null ? Set.of() : Set.copyOf(battlefieldTags);
    }
}
