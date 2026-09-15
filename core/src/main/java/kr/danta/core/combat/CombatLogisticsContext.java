package kr.danta.core.combat;

import java.util.Objects;

/** Explicit DEV-064 logistics inputs for both combat sides. */
public record CombatLogisticsContext(
        SupplyState firstSupply,
        SupplyState secondSupply,
        RetreatRouteState firstRetreatRoute,
        RetreatRouteState secondRetreatRoute
) {
    public CombatLogisticsContext {
        Objects.requireNonNull(firstSupply, "firstSupply");
        Objects.requireNonNull(secondSupply, "secondSupply");
        Objects.requireNonNull(firstRetreatRoute, "firstRetreatRoute");
        Objects.requireNonNull(secondRetreatRoute, "secondRetreatRoute");
    }

    public static CombatLogisticsContext normal() {
        return new CombatLogisticsContext(SupplyState.SUPPLIED, SupplyState.SUPPLIED,
                RetreatRouteState.OPEN, RetreatRouteState.OPEN);
    }
}
