package kr.danta.core.siege;

import java.util.Objects;
import java.util.UUID;

public record SiegeCommanderCasualty(
        String pointId,
        UUID playerId,
        SiegeSide side,
        int moraleDelta,
        boolean commanderBonusActive
) {
    public SiegeCommanderCasualty {
        Objects.requireNonNull(pointId, "pointId");
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(side, "side");
    }
}
