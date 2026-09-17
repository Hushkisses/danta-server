package kr.danta.core.combat.ai;

import kr.danta.core.combat.TroopType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CombatAiControllerSpearScreenTest {

    @Test
    void cavalryEngagesSpearmenWhenSpearScreenBlocksTheFlank() {
        CombatAiController controller = new CombatAiController();
        CombatAiObservation observation = new CombatAiObservation(
                6.0,
                TroopType.SPEARMEN,
                false,
                false,
                false,
                false,
                true,
                false,
                false);

        CombatAiDecision decision = controller.decide(CombatAiProfile.CAVALRY, observation);

        assertEquals(CombatAiAction.ENGAGE, decision.action());
        assertEquals(TroopType.SPEARMEN, decision.preferredTargetType());
    }
}
