package kr.danta.paper.combat.live;

import kr.danta.core.combat.TroopType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
