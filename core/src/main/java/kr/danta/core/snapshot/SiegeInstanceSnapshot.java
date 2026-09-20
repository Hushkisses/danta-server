package kr.danta.core.snapshot;

import kr.danta.core.siege.SiegePhase;

public record SiegeInstanceSnapshot(
        String siegeId,
        String pointId,
        String attackerNationId,
        String defenderNationId,
        SiegePhase phase,
        String result
) {}
