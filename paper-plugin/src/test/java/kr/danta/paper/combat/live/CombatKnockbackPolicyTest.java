package kr.danta.paper.combat.live;

import kr.danta.core.combat.TroopType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatKnockbackPolicyTest {

    @Test
    void developmentDefaultsDisableKnockbackForAllTrackedCombatHits() {
        CombatKnockbackPolicy policy = CombatKnockbackPolicy.developmentDefaults();

        for (TroopType attacker : TroopType.values()) {
            for (TroopType victim : TroopType.values()) {
                assertEquals(0.0, policy.multiplier(attacker, victim));
            }
        }
    }

    @Test
    void trackedDirectCombatHitsAreSuppressed() {
        CombatKnockbackPolicy policy = CombatKnockbackPolicy.developmentDefaults();

        assertTrue(policy.shouldSuppress(true, false, true));
        assertFalse(policy.shouldSuppress(true, false, false));
        assertFalse(policy.shouldSuppress(false, false, true));
    }

    @Test
    void trackedProjectileShooterAlsoSuppressesKnockback() {
        CombatKnockbackPolicy policy = CombatKnockbackPolicy.developmentDefaults();

        assertTrue(policy.shouldSuppress(false, true, true));
        assertFalse(policy.shouldSuppress(false, true, false));
    }
}
