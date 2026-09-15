package kr.danta.core.general;

import kr.danta.core.nation.NationState;
import kr.danta.core.state.GameState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Dev075GeneralCaptivityTest {
    private GameState state() {
        GameState s = new GameState();
        s.addNation(new NationState("red", "Red"));
        s.addNation(new NationState("blue", "Blue"));
        s.addGeneral(new GeneralState("g1", "red", GeneralGrade.B, 4));
        return s;
    }

    @Test void capturesForCallerSuppliedRuntimeDetentionPeriod() {
        GameState s = state();
        GeneralCaptivityState captivity = new GeneralCaptivityService(s)
                .capture("g1", "blue", 1_000L, 500L);
        assertEquals("blue", captivity.captorNationId());
        assertEquals(1_500L, captivity.detentionEndsAtRuntimeMillis());
        assertTrue(s.general("g1").orElseThrow().isCaptive());
    }

    @Test void cannotBeHeldPastRuntimeDeadline() {
        GameState s = state();
        GeneralCaptivityService service = new GeneralCaptivityService(s);
        service.capture("g1", "blue", 100L, 50L);
        assertFalse(service.releaseIfDetentionExpired("g1", 149L));
        assertTrue(service.releaseIfDetentionExpired("g1", 150L));
        assertFalse(s.general("g1").orElseThrow().isCaptive());
    }

    @Test void ransomAndExchangeReleaseRequireCaptorAuthorization() {
        GameState s = state();
        GeneralCaptivityService service = new GeneralCaptivityService(s);
        service.capture("g1", "blue", 0L, 100L);
        assertThrows(IllegalArgumentException.class, () -> service.releaseForRansom("g1", "red"));
        service.releaseForRansom("g1", "blue");
        assertFalse(s.general("g1").orElseThrow().isCaptive());

        service.capture("g1", "blue", 200L, 100L);
        service.releaseForExchange("g1", "blue");
        assertFalse(s.general("g1").orElseThrow().isCaptive());
    }

    @Test void rejectsSelfCaptureAndDuplicateCapture() {
        GameState s = state();
        GeneralCaptivityService service = new GeneralCaptivityService(s);
        assertThrows(IllegalArgumentException.class, () -> service.capture("g1", "red", 0L, 100L));
        service.capture("g1", "blue", 0L, 100L);
        assertThrows(IllegalStateException.class, () -> service.capture("g1", "blue", 1L, 100L));
    }
}
