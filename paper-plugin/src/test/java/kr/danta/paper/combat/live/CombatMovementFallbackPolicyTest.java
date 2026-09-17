package kr.danta.paper.combat.live;

import kr.danta.core.combat.ai.CombatAiAction;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatMovementFallbackPolicyTest {

    private static final UUID TARGET = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void holdAlwaysMeansHoldCurrentPositionInsteadOfReturningToAuthoredSpawnRoute() {
        assertTrue(CombatMovementFallbackPolicy.shouldHoldCurrentPosition(CombatAiAction.HOLD, TARGET));
    }

    @Test
    void noSelectedTargetHoldsCurrentPositionForOrdinaryCombatMovement() {
        assertTrue(CombatMovementFallbackPolicy.shouldHoldCurrentPosition(CombatAiAction.ADVANCE, null));
        assertTrue(CombatMovementFallbackPolicy.shouldHoldCurrentPosition(CombatAiAction.FLANK, null));
        assertTrue(CombatMovementFallbackPolicy.shouldHoldCurrentPosition(CombatAiAction.SUPPORT, null));
    }

    @Test
    void retreatStillUsesItsAuthoredRouteEvenWithoutSelectedTarget() {
        assertFalse(CombatMovementFallbackPolicy.shouldHoldCurrentPosition(CombatAiAction.RETREAT, null));
    }

    @Test
    void activeMovementWithSelectedTargetKeepsUsingNormalMovementRules() {
        assertFalse(CombatMovementFallbackPolicy.shouldHoldCurrentPosition(CombatAiAction.ADVANCE, TARGET));
        assertFalse(CombatMovementFallbackPolicy.shouldHoldCurrentPosition(CombatAiAction.FLANK, TARGET));
    }
}
