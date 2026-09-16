package kr.danta.paper.combat.live;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CombatDemoEnvironmentPolicyTest {

    @Test
    void preventsEnvironmentalCombustionOnlyForDemoOwnedEntities() {
        assertTrue(CombatDemoEnvironmentPolicy.preventCombustion(true));
        assertFalse(CombatDemoEnvironmentPolicy.preventCombustion(false));
    }
}
