package kr.danta.core.siege;

import kr.danta.core.nation.NationState;
import kr.danta.core.state.GameState;
import kr.danta.core.territory.PointPosition;
import kr.danta.core.territory.StrategicPoint;
import kr.danta.core.territory.StrategicPointType;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class Dev120SiegeRestoreTest {

    @Test
    void restoresResolvedSiegeWithoutReplayingTransitions() {
        SiegeService service = service();
        SiegeInstance restored = service.restore(
                "s1", "fort", "red", "blue", SiegePhase.RESOLVED, "ATTACKER");

        assertEquals(SiegePhase.RESOLVED, restored.phase());
        assertEquals("ATTACKER", restored.result());
        assertEquals("fort", restored.pointId());
    }

    @Test
    void restoresScheduledReservationWithOriginalWallClockInstants() {
        SiegeService sieges = service();
        sieges.restore("s1", "fort", "red", "blue", SiegePhase.SCHEDULED, null);
        SiegeReservationService reservations = reservations(sieges);

        Instant scheduledAt = Instant.parse("2026-09-20T12:00:00Z");
        Instant confirmedAt = Instant.parse("2026-09-20T10:00:00Z");
        reservations.restore(new SiegeReservation("s1", scheduledAt, confirmedAt));

        SiegeReservation restored = reservations.find("s1").orElseThrow();
        assertEquals(scheduledAt, restored.scheduledAt());
        assertEquals(confirmedAt, restored.confirmedAt());
        assertTrue(restored.confirmed());
    }

    @Test
    void clearRemovesRestoredSiegesAndReservations() {
        SiegeService sieges = service();
        sieges.restore("s1", "fort", "red", "blue", SiegePhase.SCHEDULED, null);
        SiegeReservationService reservations = reservations(sieges);
        reservations.restore(new SiegeReservation(
                "s1", Instant.parse("2026-09-20T12:00:00Z"), null));

        reservations.clear();
        sieges.clear();

        assertTrue(reservations.reservations().isEmpty());
        assertTrue(sieges.instances().isEmpty());
    }

    private static SiegeService service() {
        GameState state = new GameState();
        state.addNation(new NationState("red", "적국"));
        state.addNation(new NationState("blue", "청국"));
        state.addStrategicPoint(new StrategicPoint(
                "fort", "대요새", StrategicPointType.MAJOR, "blue",
                new PointPosition("world", 0, 64, 0), 1, Map.of()));
        return new SiegeService(state);
    }

    private static SiegeReservationService reservations(SiegeService sieges) {
        SiegeReservationService.Policy provisional = new SiegeReservationService.Policy() {
            public Duration minimumLeadTime() { return Duration.ofHours(2); }
            public Duration confirmationDeadlineBeforeStart() { return Duration.ofMinutes(30); }
        };
        return new SiegeReservationService(
                sieges, Clock.fixed(Instant.parse("2026-09-20T09:00:00Z"), ZoneOffset.UTC), provisional);
    }
}
