package kr.danta.paper.combat.live;

import kr.danta.core.combat.ai.CombatAiAction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatEngagementPolicyAdvanceTest {
    @Test
    void meleeAdvanceContinuesClosingDistancePastAuthoredRouteEndpoint() {
        assertTrue(CombatEngagementPolicy.shouldChase(
                CombatAttackPolicy.AttackMode.MELEE,
                CombatAiAction.ADVANCE,
                3.25,
                2.8));
    }

    @Test
    void meleeEngageAlsoChasesUntilInsideAttackRange() {
        assertTrue(CombatEngagementPolicy.shouldChase(
                CombatAttackPolicy.AttackMode.MELEE,
                CombatAiAction.ENGAGE,
                3.25,
                2.8));
    }

    @Test
    void nonClosingActionsDoNotOverrideTheirTacticalRoute() {
        assertFalse(CombatEngagementPolicy.shouldChase(
                CombatAttackPolicy.AttackMode.MELEE,
                CombatAiAction.SCREEN,
                6.0,
                3.2));
        assertFalse(CombatEngagementPolicy.shouldChase(
                CombatAttackPolicy.AttackMode.RANGED,
                CombatAiAction.ADVANCE,
                20.0,
                14.0));
    }
}
