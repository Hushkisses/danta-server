package kr.danta.core.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Dev062MoraleRetreatPursuitTest {
    private final CombatResolverV1 resolver = new CombatResolverV1();
    private final MoraleRetreatPursuitPolicy policy = new MoraleRetreatPursuitPolicy();

    @Test void designMoraleStagesExistInDeclaredOrder() {
        assertArrayEquals(new MoraleState[]{
                MoraleState.INSPIRED, MoraleState.HIGH, MoraleState.NORMAL,
                MoraleState.LOW, MoraleState.NEAR_COLLAPSE
        }, MoraleState.values());
    }

    @Test void defeatedSideRoutesInsteadOfBeingAnnihilated() {
        CombatV1Result combat = resolver.resolve(
                CombatSideInput.neutral("red", TroopType.INFANTRY, 1000),
                CombatSideInput.neutral("blue", TroopType.SPEARMEN, 1000));
        CombatResolutionV1 result = policy.apply(combat, RetreatPursuitContext.normal());

        assertFalse(result.first().routed());
        assertTrue(result.second().routed());
        assertEquals(750, result.losses().second().remainingTroops());
        assertTrue(result.losses().second().remainingTroops() > 0);
    }

    @Test void winningCavalryGetsPursuitAdvantageAgainstRoutedEnemy() {
        CombatV1Result combat = resolver.resolve(
                CombatSideInput.neutral("red", TroopType.CAVALRY, 1000),
                CombatSideInput.neutral("blue", TroopType.ARCHERS, 1000));
        CombatResolutionV1 result = policy.apply(combat, RetreatPursuitContext.normal());

        assertTrue(result.second().routed());
        assertTrue(result.first().pursuitAdvantageAgainstOpponent());
        assertFalse(result.second().pursuitAdvantageAgainstOpponent());
    }

    @Test void winningNonCavalryDoesNotInventPursuitCasualtyBonus() {
        CombatV1Result combat = resolver.resolve(
                CombatSideInput.neutral("red", TroopType.INFANTRY, 1000),
                CombatSideInput.neutral("blue", TroopType.SPEARMEN, 1000));
        CombatResolutionV1 result = policy.apply(combat, RetreatPursuitContext.normal());

        assertFalse(result.first().pursuitAdvantageAgainstOpponent());
        assertEquals(250, result.losses().second().losses());
    }

    @Test void moraleStateIsCarriedWithoutUnfixedNumericMultiplier() {
        CombatV1Result combat = resolver.resolve(
                CombatSideInput.neutral("red", TroopType.INFANTRY, 1000),
                CombatSideInput.neutral("blue", TroopType.SPEARMEN, 1000));
        CombatResolutionV1 result = policy.apply(combat,
                new RetreatPursuitContext(MoraleState.HIGH, MoraleState.NEAR_COLLAPSE));

        assertEquals(MoraleState.HIGH, result.first().morale());
        assertEquals(MoraleState.NEAR_COLLAPSE, result.second().morale());
        assertEquals(1250.0, result.combat().finalResult().first().effectivePower(), 0.000001);
        assertEquals(1000.0, result.combat().finalResult().second().effectivePower(), 0.000001);
    }

    @Test void drawDoesNotRouteOrCreatePursuit() {
        CombatV1Result combat = resolver.resolve(
                CombatSideInput.neutral("red", TroopType.CAVALRY, 1000),
                CombatSideInput.neutral("blue", TroopType.CAVALRY, 1000));
        CombatResolutionV1 result = policy.apply(combat, RetreatPursuitContext.normal());

        assertFalse(result.first().routed());
        assertFalse(result.second().routed());
        assertFalse(result.first().pursuitAdvantageAgainstOpponent());
        assertFalse(result.second().pursuitAdvantageAgainstOpponent());
    }
}
