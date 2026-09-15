package kr.danta.core.economy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Dev052EconomyTickTest {
    @Test void ticksOnlyWhenThirtyRuntimeMinutesAreCrossed() {
        EconomyTickService ticks = new EconomyTickService(0);
        assertEquals(0, ticks.claimDueTicks(29 * 60_000L + 59_999L));
        assertEquals(1, ticks.claimDueTicks(30 * 60_000L));
        assertEquals(0, ticks.claimDueTicks(30 * 60_000L));
        assertEquals(1, ticks.claimDueTicks(60 * 60_000L));
    }
    @Test void restartAtCurrentRuntimeDoesNotReplayOldTicks() {
        EconomyTickService ticks = new EconomyTickService(95 * 60_000L);
        assertEquals(0, ticks.claimDueTicks(95 * 60_000L));
        assertEquals(1, ticks.claimDueTicks(120 * 60_000L));
    }
    @Test void speedJumpCanClaimMultipleBoundariesExactlyOnce() {
        EconomyTickService ticks = new EconomyTickService(0);
        assertEquals(3, ticks.claimDueTicks(90 * 60_000L));
        assertEquals(0, ticks.claimDueTicks(90 * 60_000L));
    }
}
