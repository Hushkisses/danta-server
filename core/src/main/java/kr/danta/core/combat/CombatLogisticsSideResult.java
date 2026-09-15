package kr.danta.core.combat;

import java.util.Objects;

/**
 * DEV-064 diagnostics. Flags express authoritative design consequences without
 * inventing final percentage modifiers.
 */
public record CombatLogisticsSideResult(
        String sideId,
        SupplyState supply,
        RetreatRouteState retreatRoute,
        boolean isolated,
        boolean moralePenaltyRequired,
        boolean combatPenaltyRequired,
        boolean additionalLossRisk,
        boolean surrenderRisk
) {
    public CombatLogisticsSideResult {
        Objects.requireNonNull(sideId, "sideId");
        Objects.requireNonNull(supply, "supply");
        Objects.requireNonNull(retreatRoute, "retreatRoute");
        if (sideId.isBlank()) throw new IllegalArgumentException("sideId must not be blank");
    }
}
