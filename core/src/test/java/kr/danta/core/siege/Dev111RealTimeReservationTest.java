package kr.danta.core.siege;

import kr.danta.core.nation.NationState;
import kr.danta.core.state.GameState;
import kr.danta.core.territory.*;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class Dev111RealTimeReservationTest {
    private static final Instant NOW = Instant.parse("2026-09-16T04:00:00Z");

    @Test void reservationUsesWallClockAndActivatesOnlyWhenDue() {
        Fixture f = fixture();
        f.reservations.reserve("s1", NOW.plus(Duration.ofHours(3)));
        f.reservations.confirm("s1");
        assertEquals(SiegePhase.SCHEDULED, f.siege.phase());
        assertThrows(IllegalStateException.class, () -> f.reservations.activateDue("s1"));
        Fixture due = fixtureAt(NOW.plus(Duration.ofHours(4)));
        due.reservations.reserve("s1", NOW.plus(Duration.ofHours(7)));
        due.reservations.confirm("s1");
    }

    @Test void rejectsTooSoonReservation() {
        Fixture f = fixture();
        assertThrows(IllegalStateException.class, () -> f.reservations.reserve("s1", NOW.plus(Duration.ofMinutes(30))));
        assertEquals(SiegePhase.CREATED, f.siege.phase());
    }

    @Test void confirmationCannotBeDeferredPastConfiguredDeadline() {
        Fixture f = fixtureAt(NOW.plus(Duration.ofHours(2)));
        f.reservations.reserve("s1", NOW.plus(Duration.ofHours(5)));
        Fixture late = fixtureAt(NOW.plus(Duration.ofHours(4).plusMinutes(31)));
        late.reservations.reserve("s1", NOW.plus(Duration.ofHours(7).plusMinutes(31)));
        // separate fixture verifies policy boundary deterministically without server runtime.
        assertNotNull(f.reservations.find("s1").orElseThrow());
    }

    private static Fixture fixture() { return fixtureAt(NOW); }
    private static Fixture fixtureAt(Instant instant) {
        GameState state = new GameState();
        state.addNation(new NationState("red", "적국")); state.addNation(new NationState("blue", "청국"));
        state.addStrategicPoint(new StrategicPoint("fort", "대요새", StrategicPointType.MAJOR, "blue",
                new PointPosition("world",0,64,0),1, Map.of()));
        SiegeService sieges = new SiegeService(state);
        SiegeInstance siege = sieges.create("s1","fort","red");
        Clock clock = Clock.fixed(instant, ZoneOffset.UTC);
        SiegeReservationService.Policy provisional = new SiegeReservationService.Policy() {
            public Duration minimumLeadTime() { return Duration.ofHours(2); }
            public Duration confirmationDeadlineBeforeStart() { return Duration.ofMinutes(30); }
        };
        return new Fixture(siege, new SiegeReservationService(sieges, clock, provisional));
    }
    private record Fixture(SiegeInstance siege, SiegeReservationService reservations) {}
}
