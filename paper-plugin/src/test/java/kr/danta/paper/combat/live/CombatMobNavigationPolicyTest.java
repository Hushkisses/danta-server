package kr.danta.paper.combat.live;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatMobNavigationPolicyTest {

    @Test
    void convertsLegacyStepSpeedIntoUsablePathfinderMultiplier() {
        assertEquals(0.96, CombatMobNavigationPolicy.pathfinderSpeedMultiplier(0.24), 0.0001);
        assertEquals(0.80, CombatMobNavigationPolicy.pathfinderSpeedMultiplier(0.10), 0.0001);
        assertEquals(1.28, CombatMobNavigationPolicy.pathfinderSpeedMultiplier(0.32), 0.0001);
    }

    @Test
    void onlyDemoOwnedMobsHaveVanillaTargetingSuppressed() {
        assertTrue(CombatMobNavigationPolicy.shouldSuppressVanillaTargeting(true));
        assertFalse(CombatMobNavigationPolicy.shouldSuppressVanillaTargeting(false));
    }
}
