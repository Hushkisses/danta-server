package kr.danta.core.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Dev064CombatLogisticsTest {
    private final CombatLogisticsPolicy policy = new CombatLogisticsPolicy();

    @Test void suppliedOpenRouteHasNoLogisticsPenaltyFlags() {
        CombatLogisticsSideResult r = policy.evaluate("red", SupplyState.SUPPLIED, RetreatRouteState.OPEN);
        assertFalse(r.isolated());
        assertFalse(r.moralePenaltyRequired());
        assertFalse(r.combatPenaltyRequired());
        assertFalse(r.additionalLossRisk());
        assertFalse(r.surrenderRisk());
    }

    @Test void lowSupplyRequiresMoraleAndCombatPenaltyWithoutInventingRate() {
        CombatLogisticsSideResult r = policy.evaluate("red", SupplyState.LOW, RetreatRouteState.OPEN);
        assertTrue(r.moralePenaltyRequired());
        assertTrue(r.combatPenaltyRequired());
        assertFalse(r.additionalLossRisk());
        assertFalse(r.surrenderRisk());
    }

    @Test void depletedSupplyAddsLossRisk() {
        CombatLogisticsSideResult r = policy.evaluate("red", SupplyState.DEPLETED, RetreatRouteState.OPEN);
        assertTrue(r.moralePenaltyRequired());
        assertTrue(r.combatPenaltyRequired());
        assertTrue(r.additionalLossRisk());
        assertFalse(r.surrenderRisk());
    }

    @Test void blockedRetreatRouteMarksIsolationAndLossRisk() {
        CombatLogisticsSideResult r = policy.evaluate("red", SupplyState.SUPPLIED, RetreatRouteState.BLOCKED);
        assertTrue(r.isolated());
        assertTrue(r.additionalLossRisk());
        assertFalse(r.surrenderRisk());
    }

    @Test void depletedAndBlockedCreatesSurrenderRisk() {
        CombatLogisticsSideResult r = policy.evaluate("red", SupplyState.DEPLETED, RetreatRouteState.BLOCKED);
        assertTrue(r.isolated());
        assertTrue(r.additionalLossRisk());
        assertTrue(r.surrenderRisk());
    }

    @Test void evaluatesBothSidesAgainstStagedCombatResult() {
        CombatV1Result combat = new CombatResolverV1().resolve(
                CombatSideInput.neutral("red", TroopType.INFANTRY, 1000),
                CombatSideInput.neutral("blue", TroopType.SPEARMEN, 1000));
        CombatLogisticsResult result = policy.evaluate(combat,
                new CombatLogisticsContext(SupplyState.SUPPLIED, SupplyState.DEPLETED,
                        RetreatRouteState.OPEN, RetreatRouteState.BLOCKED));
        assertFalse(result.first().isolated());
        assertTrue(result.second().isolated());
        assertTrue(result.second().surrenderRisk());
    }
}
