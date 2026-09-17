package kr.danta.core.combat.ai;

import kr.danta.core.combat.TroopType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Dev114CombatAiControllerTest {
    private final CombatAiController controller = new CombatAiController();

    @Test
    void lowHealthDoesNotAutomaticallyRetreatInCurrentCombatModel() {
        CombatAiDecision decision = controller.decide(CombatAiProfile.INFANTRY,
                obs(2.0, TroopType.SPEARMEN, true, false, false, false, false, true, true));
        assertEquals(CombatAiAction.ENGAGE, decision.action());
    }

    @Test
    void infantryEngagesAtCloseRangeAndOtherwiseAdvances() {
        CombatAiDecision close = controller.decide(CombatAiProfile.INFANTRY,
                obs(2.5, TroopType.INFANTRY, true, false, false, false, false, false, true));
        CombatAiDecision far = controller.decide(CombatAiProfile.INFANTRY,
                obs(8.0, TroopType.INFANTRY, true, false, false, false, false, false, true));

        assertEquals(CombatAiAction.ENGAGE, close.action());
        assertEquals(CombatAiAction.ADVANCE, far.action());
        assertNotEquals(CombatAiAction.FLANK, far.action());
        assertNotEquals(CombatAiAction.PURSUE, far.action());
    }

    @Test
    void spearmenPrioritizeCavalryThreat() {
        CombatAiDecision decision = controller.decide(CombatAiProfile.SPEARMEN,
                obs(4.0, TroopType.CAVALRY, true, false, false, false, false, false, true));

        assertEquals(CombatAiAction.SCREEN, decision.action());
        assertEquals(TroopType.CAVALRY, decision.preferredTargetType());
    }

    @Test
    void spearmenScreenThreatenedBackline() {
        CombatAiDecision decision = controller.decide(CombatAiProfile.SPEARMEN,
                obs(5.0, TroopType.INFANTRY, true, true, false, false, false, false, true));

        assertEquals(CombatAiAction.SCREEN, decision.action());
    }

    @Test
    void archersEngageEvenWithoutFrontlineSoRangedEndgameCanResolve() {
        CombatAiDecision withFrontline = controller.decide(CombatAiProfile.ARCHERS,
                obs(10.0, TroopType.INFANTRY, true, false, false, false, false, false, true));
        CombatAiDecision withoutFrontline = controller.decide(CombatAiProfile.ARCHERS,
                obs(20.0, TroopType.MAGIC, false, false, false, false, false, false, false));

        assertEquals(CombatAiAction.ENGAGE, withFrontline.action());
        assertEquals(CombatAiAction.ENGAGE, withoutFrontline.action());
    }

    @Test
    void cavalryFlanksExposedBacklineAndPursuesRetreatingTarget() {
        CombatAiDecision flank = controller.decide(CombatAiProfile.CAVALRY,
                obs(9.0, TroopType.ARCHERS, true, false, true, false, false, false, true));
        CombatAiDecision pursue = controller.decide(CombatAiProfile.CAVALRY,
                obs(9.0, TroopType.INFANTRY, true, false, false, true, false, false, true));

        assertEquals(CombatAiAction.FLANK, flank.action());
        assertEquals(CombatAiAction.PURSUE, pursue.action());
    }

    @Test
    void cavalryEngagesBlockingSpearScreen() {
        CombatAiDecision decision = controller.decide(CombatAiProfile.CAVALRY,
                obs(5.0, TroopType.SPEARMEN, true, false, false, false, true, false, true));

        assertEquals(CombatAiAction.ENGAGE, decision.action());
        assertEquals(TroopType.SPEARMEN, decision.preferredTargetType());
    }

    @Test
    void magicSupportsAlliesButEngagesWhenItIsTheRemainingCombatLine() {
        CombatAiDecision support = controller.decide(CombatAiProfile.MAGIC,
                obs(10.0, TroopType.INFANTRY, true, false, false, false, false, false, true));
        CombatAiDecision threatened = controller.decide(CombatAiProfile.MAGIC,
                obs(3.0, TroopType.CAVALRY, true, true, false, false, false, false, true));
        CombatAiDecision endgame = controller.decide(CombatAiProfile.MAGIC,
                obs(12.0, TroopType.ARCHERS, false, false, false, false, false, false, false));

        assertEquals(CombatAiAction.SUPPORT, support.action());
        assertEquals(CombatAiAction.SUPPORT, threatened.action());
        assertEquals(CombatAiAction.ENGAGE, endgame.action());
    }

    @Test
    void identicalInputsProduceEqualDecisions() {
        CombatAiObservation observation = obs(6.0, TroopType.CAVALRY,
                true, false, false, false, false, false, true);

        CombatAiDecision first = controller.decide(CombatAiProfile.SPEARMEN, observation);
        CombatAiDecision second = controller.decide(CombatAiProfile.SPEARMEN, observation);

        assertEquals(first, second);
    }

    private static CombatAiObservation obs(
            double nearestHostileDistance,
            TroopType nearestHostileType,
            boolean frontlineSupportPresent,
            boolean backlineThreatened,
            boolean exposedEnemyBackline,
            boolean hostileRetreating,
            boolean spearScreenPresent,
            boolean survivalThreatened,
            boolean alliedCombatGroupPresent
    ) {
        return new CombatAiObservation(
                nearestHostileDistance,
                nearestHostileType,
                frontlineSupportPresent,
                backlineThreatened,
                exposedEnemyBackline,
                hostileRetreating,
                spearScreenPresent,
                survivalThreatened,
                alliedCombatGroupPresent);
    }
}
