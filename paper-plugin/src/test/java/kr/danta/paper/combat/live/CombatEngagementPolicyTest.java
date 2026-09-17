package kr.danta.paper.combat.live;

import kr.danta.core.combat.TroopType;
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
    void cavalryAndSpearmenMayCloseOnEachOtherDuringFlankScreenActions() {
        assertTrue(CombatEngagementPolicy.shouldChasePriorityMatchup(
                CombatAttackPolicy.AttackMode.MELEE,
                TroopType.CAVALRY,
                TroopType.SPEARMEN,
                CombatAiAction.FLANK,
                18.0,
                3.0));
        assertTrue(CombatEngagementPolicy.shouldChasePriorityMatchup(
                CombatAttackPolicy.AttackMode.MELEE,
                TroopType.SPEARMEN,
                TroopType.CAVALRY,
                CombatAiAction.SCREEN,
                18.0,
                3.2));

        assertFalse(CombatEngagementPolicy.shouldChasePriorityMatchup(
                CombatAttackPolicy.AttackMode.MELEE,
                TroopType.INFANTRY,
                TroopType.SPEARMEN,
                CombatAiAction.SCREEN,
                18.0,
                2.8));
        assertFalse(CombatEngagementPolicy.shouldChasePriorityMatchup(
                CombatAttackPolicy.AttackMode.MELEE,
                TroopType.CAVALRY,
                TroopType.SPEARMEN,
                CombatAiAction.RETREAT,
                18.0,
                3.0));
    }

    @Test
    void rangedUnitsHoldPositionOnceSelectedTargetIsInsideTheirRange() {
        assertTrue(CombatEngagementPolicy.shouldHoldRangedPosition(
                TroopType.ARCHERS,
                CombatAttackPolicy.AttackMode.RANGED,
                CombatAiAction.ENGAGE,
                12.0,
                14.0));
        assertTrue(CombatEngagementPolicy.shouldHoldRangedPosition(
                TroopType.MAGIC,
                CombatAttackPolicy.AttackMode.RANGED,
                CombatAiAction.ENGAGE,
                8.0,
                9.0));
        assertTrue(CombatEngagementPolicy.shouldHoldRangedPosition(
                TroopType.MAGIC,
                CombatAttackPolicy.AttackMode.RANGED,
                CombatAiAction.SUPPORT,
                8.0,
                9.0));
        assertFalse(CombatEngagementPolicy.shouldHoldRangedPosition(
                TroopType.ARCHERS,
                CombatAttackPolicy.AttackMode.RANGED,
                CombatAiAction.ENGAGE,
                15.0,
                14.0));
    }

    @Test
    void rangedUnitsCloseOnSelectedTargetUntilInsideTheirOwnFiringRange() {
        assertTrue(CombatEngagementPolicy.shouldChaseRangedTarget(
                CombatAttackPolicy.AttackMode.RANGED,
                CombatAiAction.ENGAGE,
                20.0,
                14.0));
        assertTrue(CombatEngagementPolicy.shouldChaseRangedTarget(
                CombatAttackPolicy.AttackMode.RANGED,
                CombatAiAction.ENGAGE,
                12.0,
                9.0));
        assertFalse(CombatEngagementPolicy.shouldChaseRangedTarget(
                CombatAttackPolicy.AttackMode.RANGED,
                CombatAiAction.ENGAGE,
                8.0,
                9.0));
        assertTrue(CombatEngagementPolicy.shouldChaseRangedTarget(
                CombatAttackPolicy.AttackMode.RANGED,
                CombatAiAction.SUPPORT,
                20.0,
                9.0));
    }

    @Test
    void cavalryDirectlyChasesArchersAndMagicAfterBreakingTheSpearScreen() {
        assertTrue(CombatEngagementPolicy.shouldChaseBacklineTarget(
                CombatAttackPolicy.AttackMode.MELEE,
                TroopType.CAVALRY,
                TroopType.ARCHERS,
                CombatAiAction.FLANK,
                18.0,
                3.0));
        assertTrue(CombatEngagementPolicy.shouldChaseBacklineTarget(
                CombatAttackPolicy.AttackMode.MELEE,
                TroopType.CAVALRY,
                TroopType.MAGIC,
                CombatAiAction.FLANK,
                18.0,
                3.0));
        assertFalse(CombatEngagementPolicy.shouldChaseBacklineTarget(
                CombatAttackPolicy.AttackMode.MELEE,
                TroopType.CAVALRY,
                TroopType.INFANTRY,
                CombatAiAction.FLANK,
                18.0,
                3.0));
        assertFalse(CombatEngagementPolicy.shouldChaseBacklineTarget(
                CombatAttackPolicy.AttackMode.MELEE,
                TroopType.INFANTRY,
                TroopType.ARCHERS,
                CombatAiAction.ADVANCE,
                18.0,
                2.8));
    }

    @Test
    void rangedAndSupportUnitsDoNotUseMeleeChaseOverride() {
        assertFalse(CombatEngagementPolicy.shouldChase(
                CombatAttackPolicy.AttackMode.RANGED, 20.0, 14.0));
        assertFalse(CombatEngagementPolicy.shouldChase(
                CombatAttackPolicy.AttackMode.SUPPORT_VISUAL, 20.0, 9.0));
    }
}
