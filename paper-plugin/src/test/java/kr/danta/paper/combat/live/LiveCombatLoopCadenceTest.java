package kr.danta.paper.combat.live;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LiveCombatLoopCadenceTest {

    @Test
    void movementRunsMoreOftenThanCombatAiDecisionReevaluation() {
        LiveCombatLoopCadence cadence = new LiveCombatLoopCadence(2, 10);

        assertFalse(cadence.advance().movementDue());
        assertTrue(cadence.advance().movementDue());

        for (int i = 0; i < 7; i++) {
            assertFalse(cadence.advance().decisionDue());
        }
        LiveCombatLoopCadence.TickSchedule tenth = cadence.advance();
        assertTrue(tenth.movementDue());
        assertTrue(tenth.decisionDue());
    }
}
