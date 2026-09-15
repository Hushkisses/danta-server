package kr.danta.core.combat;

import java.util.Objects;

/**
 * DEV-064 supply/isolation/retreat-route policy.
 *
 * Design v0.3 requires low/depleted supply to harm combat/morale and blocked
 * retreat to increase permanent-loss/surrender risk. Exact coefficients are not
 * fixed, so this policy exposes required consequences as typed flags.
 */
public final class CombatLogisticsPolicy {
    public CombatLogisticsSideResult evaluate(String sideId, SupplyState supply, RetreatRouteState route) {
        Objects.requireNonNull(sideId, "sideId");
        Objects.requireNonNull(supply, "supply");
        Objects.requireNonNull(route, "route");

        boolean lowOrWorse = supply != SupplyState.SUPPLIED;
        boolean depleted = supply == SupplyState.DEPLETED;
        boolean isolated = route == RetreatRouteState.BLOCKED;
        boolean additionalLossRisk = isolated || depleted;
        boolean surrenderRisk = isolated && depleted;

        return new CombatLogisticsSideResult(sideId, supply, route, isolated,
                lowOrWorse, lowOrWorse, additionalLossRisk, surrenderRisk);
    }

    public CombatLogisticsResult evaluate(CombatV1Result combat, CombatLogisticsContext context) {
        Objects.requireNonNull(combat, "combat");
        Objects.requireNonNull(context, "context");
        return new CombatLogisticsResult(
                evaluate(combat.finalResult().first().sideId(), context.firstSupply(), context.firstRetreatRoute()),
                evaluate(combat.finalResult().second().sideId(), context.secondSupply(), context.secondRetreatRoute())
        );
    }
}
