package kr.danta.core.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Dev042LossRetreatModelTest {
    private final CombatResolver resolver = new CombatResolver();
    private final CombatLossPolicy policy = new CombatLossPolicy();

    @Test void winnerLosesTenPercentAndLoserLosesTwentyFiveThenRetreats() {
        CombatResult power = resolver.resolve(
                CombatSideInput.neutral("red", TroopType.INFANTRY, 1000),
                CombatSideInput.neutral("blue", TroopType.SPEARMEN, 1000));
        CombatResolution resolution = policy.apply(power);

        assertEquals(100, resolution.first().losses());
        assertEquals(900, resolution.first().remainingTroops());
        assertEquals(CombatOutcome.VICTORY, resolution.first().outcome());
        assertFalse(resolution.first().retreatRequired());

        assertEquals(250, resolution.second().losses());
        assertEquals(750, resolution.second().remainingTroops());
        assertEquals(CombatOutcome.DEFEAT, resolution.second().outcome());
        assertTrue(resolution.second().retreatRequired());
    }

    @Test void drawUsesSymmetricTemporaryLossWithoutForcedRetreat() {
        CombatResult power = resolver.resolve(
                CombatSideInput.neutral("red", TroopType.INFANTRY, 1000),
                CombatSideInput.neutral("blue", TroopType.INFANTRY, 1000));
        CombatResolution resolution = policy.apply(power);
        assertEquals(100, resolution.first().losses());
        assertEquals(100, resolution.second().losses());
        assertEquals(CombatOutcome.DRAW, resolution.first().outcome());
        assertEquals(CombatOutcome.DRAW, resolution.second().outcome());
        assertFalse(resolution.first().retreatRequired());
        assertFalse(resolution.second().retreatRequired());
    }

    @Test void lossesAreRoundedAndNeverExceedInitialTroops() {
        CombatResult power = resolver.resolve(
                CombatSideInput.neutral("red", TroopType.ARCHERS, 3),
                CombatSideInput.neutral("blue", TroopType.INFANTRY, 3));
        CombatResolution resolution = policy.apply(power);
        assertEquals(0, resolution.first().losses());
        assertEquals(1, resolution.second().losses());
        assertTrue(resolution.second().remainingTroops() >= 0);
    }
}
