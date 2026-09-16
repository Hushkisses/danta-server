package kr.danta.paper.combat.live;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LiveCombatRuntimeStateTest {

    @Test
    void repeatedStartIsRejectedAndStopClearsActiveState() {
        LiveCombatRuntimeState state = new LiveCombatRuntimeState();

        assertTrue(state.begin());
        assertTrue(state.active());
        assertFalse(state.begin());

        state.finish();
        assertFalse(state.active());
        assertTrue(state.begin());
    }

    @Test
    void cavalryMemberLossRetiresWholeLogicalUnit() {
        UUID rider = UUID.randomUUID();
        UUID mount = UUID.randomUUID();
        CavalryRetirementPolicy policy = new CavalryRetirementPolicy();

        CavalryRetirementPolicy.RetirementRequest request =
                policy.evaluate(new CavalryCompositeState(rider, mount), false, true).orElseThrow();

        assertTrue(request.entityIds().contains(rider));
        assertTrue(request.entityIds().contains(mount));
    }

    @Test
    void attackCooldownIsTrackedPerUnitAndClearedWhenRuntimeFinishes() {
        LiveCombatRuntimeState state = new LiveCombatRuntimeState();
        UUID unitA = UUID.randomUUID();
        UUID unitB = UUID.randomUUID();

        assertTrue(state.tryAcquireAttack(unitA, 1_000L, 500L));
        assertFalse(state.tryAcquireAttack(unitA, 1_499L, 500L));
        assertTrue(state.tryAcquireAttack(unitB, 1_100L, 500L));
        assertTrue(state.tryAcquireAttack(unitA, 1_500L, 500L));

        state.finish();
        assertTrue(state.tryAcquireAttack(unitA, 1_501L, 500L));
    }
}
