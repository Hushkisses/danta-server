package kr.danta.paper.combat.live;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CavalryCompositeStateTest {

    @Test
    void cavalryRetiresWhenEitherCompositeMemberIsGone() {
        CavalryCompositeState state = new CavalryCompositeState(
                UUID.randomUUID(),
                UUID.randomUUID());

        assertFalse(state.shouldRetire(true, true));
        assertTrue(state.shouldRetire(false, true));
        assertTrue(state.shouldRetire(true, false));
    }
}
