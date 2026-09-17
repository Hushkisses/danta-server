package kr.danta.paper.combat.live;

import kr.danta.core.combat.ai.CombatAiAction;
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
    void onlyDirectClosingActionsOverrideAuthoredRoute() {
        assertTrue(CombatEngagementPolicy.shouldChase(
                CombatAttackPolicy.AttackMode.MELEE, CombatAiAction.ADVANCE, 8.0, 3.0));
        assertTrue(CombatEngagementPolicy.shouldChase(
                CombatAttackPolicy.AttackMode.MELEE, CombatAiAction.ENGAGE, 8.0, 3.0));

        assertFalse(CombatEngagementPolicy.shouldChase(
                CombatAttackPolicy.AttackMode.MELEE, CombatAiAction.SCREEN, 8.0, 3.0));
        assertFalse(CombatEngagementPolicy.shouldChase(
                CombatAttackPolicy.AttackMode.MELEE, CombatAiAction.FLANK, 8.0, 3.0));
        assertFalse(CombatEngagementPolicy.shouldChase(
                CombatAttackPolicy.AttackMode.MELEE, CombatAiAction.PURSUE, 8.0, 3.0));
        assertFalse(CombatEngagementPolicy.shouldChase(
                CombatAttackPolicy.AttackMode.MELEE, CombatAiAction.HOLD, 8.0, 3.0));
        assertFalse(CombatEngagementPolicy.shouldChase(
                CombatAttackPolicy.AttackMode.MELEE, CombatAiAction.RETREAT, 8.0, 3.0));
    }

    @Test
    void rangedAndSupportUnitsDoNotUseMeleeChaseOverride() {
        assertFalse(CombatEngagementPolicy.shouldChase(
                CombatAttackPolicy.AttackMode.RANGED, 20.0, 14.0));
        assertFalse(CombatEngagementPolicy.shouldChase(
                CombatAttackPolicy.AttackMode.SUPPORT_VISUAL, 20.0, 9.0));
    }
}
