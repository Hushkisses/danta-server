package kr.danta.core.general;

import kr.danta.core.nation.NationState;
import kr.danta.core.state.GameState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Dev074GeneralRecoveryTest {
    private GameState state() {
        GameState state = new GameState();
        state.addNation(new NationState("red", "Red"));
        state.addGeneral(new GeneralState("g1", "red", GeneralGrade.B, 4));
        return state;
    }

    @Test void injuryUsesCallerSuppliedRuntimeDeadline() {
        GameState state = state();
        GeneralRecoveryService service = new GeneralRecoveryService(state);
        GeneralRecoveryState recovery = service.injure(
                "g1", GeneralHealthStatus.INJURED, 10_000L, 5_000L);

        assertEquals(10_000L, recovery.injuredAtRuntimeMillis());
        assertEquals(15_000L, recovery.recoveryReadyAtRuntimeMillis());
        assertEquals(GeneralHealthStatus.INJURED, state.general("g1").orElseThrow().healthStatus());
    }

    @Test void severeInjuryIsDistinctAndDoesNotInventDuration() {
        GameState state = state();
        GeneralRecoveryService service = new GeneralRecoveryService(state);
        service.injure("g1", GeneralHealthStatus.SEVERELY_INJURED, 20L, 123L);
        GeneralRecoveryState r = state.general("g1").orElseThrow().recoveryState().orElseThrow();
        assertEquals(GeneralHealthStatus.SEVERELY_INJURED, r.status());
        assertEquals(143L, r.recoveryReadyAtRuntimeMillis());
    }

    @Test void cannotRecoverBeforeRuntimeDeadlineButCanAtDeadline() {
        GameState state = state();
        GeneralRecoveryService service = new GeneralRecoveryService(state);
        service.injure("g1", GeneralHealthStatus.INJURED, 100L, 50L);

        assertFalse(service.recoverIfReady("g1", 149L));
        assertTrue(service.recoverIfReady("g1", 150L));
        assertEquals(GeneralHealthStatus.HEALTHY, state.general("g1").orElseThrow().healthStatus());
        assertFalse(service.recoverIfReady("g1", 151L));
    }

    @Test void rejectsHealthyAsInjuryAndNonPositiveDuration() {
        GeneralRecoveryService service = new GeneralRecoveryService(state());
        assertThrows(IllegalArgumentException.class,
                () -> service.injure("g1", GeneralHealthStatus.HEALTHY, 0L, 10L));
        assertThrows(IllegalArgumentException.class,
                () -> service.injure("g1", GeneralHealthStatus.INJURED, 0L, 0L));
    }
}
