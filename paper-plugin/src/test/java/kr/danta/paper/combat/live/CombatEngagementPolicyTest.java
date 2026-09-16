package kr.danta.paper.combat.live;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CombatEngagementPolicyTest {

    @Test
    void meleeUnitsChaseUntilInsideAttackRange() {
        assertTrue(CombatEngagementPolicy.shouldChase(
                CombatAttackPolicy.AttackMode.MELEE, 4.0, 2.8));
        assertFalse(CombatEngagementPolicy.shouldChase(
                CombatAttackPolicy.AttackMode.MELEE, 2.7, 2.8));
    }

    @Test
    void rangedAndSupportUnitsDoNotUseMeleeChaseOverride() {
        assertFalse(CombatEngagementPolicy.shouldChase(
                CombatAttackPolicy.AttackMode.RANGED, 20.0, 14.0));
        assertFalse(CombatEngagementPolicy.shouldChase(
                CombatAttackPolicy.AttackMode.SUPPORT_VISUAL, 20.0, 9.0));
    }
}
