package kr.danta.core.combat.ai;

import kr.danta.core.combat.TroopType;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

class Dev114CombatAiModelTest {
    @Test
    void definesFiveProfilesAndEightActions() {
        assertEquals(EnumSet.of(
                CombatAiProfile.INFANTRY,
                CombatAiProfile.SPEARMEN,
                CombatAiProfile.ARCHERS,
                CombatAiProfile.CAVALRY,
                CombatAiProfile.MAGIC), EnumSet.allOf(CombatAiProfile.class));

        assertEquals(EnumSet.of(
                CombatAiAction.HOLD,
                CombatAiAction.ADVANCE,
                CombatAiAction.ENGAGE,
                CombatAiAction.RETREAT,
                CombatAiAction.SCREEN,
                CombatAiAction.FLANK,
                CombatAiAction.PURSUE,
                CombatAiAction.SUPPORT), EnumSet.allOf(CombatAiAction.class));
    }

    @Test
    void observationKeepsOnlyAbstractTacticalFacts() {
        CombatAiObservation observation = new CombatAiObservation(
                6.5,
                TroopType.CAVALRY,
                true,
                false,
                true,
                false,
                true,
                false,
                true);

        assertEquals(6.5, observation.nearestHostileDistance(), 0.000001);
        assertEquals(TroopType.CAVALRY, observation.nearestHostileType());
        assertTrue(observation.frontlineSupportPresent());
        assertTrue(observation.exposedEnemyBackline());
        assertTrue(observation.spearScreenPresent());
        assertTrue(observation.alliedCombatGroupPresent());
    }

    @Test
    void observationRejectsInvalidDistance() {
        assertThrows(IllegalArgumentException.class, () -> new CombatAiObservation(
                -0.1,
                TroopType.INFANTRY,
                false,
                false,
                false,
                false,
                false,
                false,
                false));

        assertThrows(IllegalArgumentException.class, () -> new CombatAiObservation(
                Double.NaN,
                TroopType.INFANTRY,
                false,
                false,
                false,
                false,
                false,
                false,
                false));
    }

    @Test
    void decisionRequiresActionButTargetPreferenceCanBeAbsent() {
        CombatAiDecision decision = new CombatAiDecision(CombatAiAction.HOLD, null);
        assertEquals(CombatAiAction.HOLD, decision.action());
        assertNull(decision.preferredTargetType());

        assertThrows(NullPointerException.class, () -> new CombatAiDecision(null, TroopType.INFANTRY));
    }
}
