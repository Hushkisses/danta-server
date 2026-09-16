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
        MutableClock clock = new MutableClock(NOW);
        Fixture f = fixtureWithClock(clock);
        f.reservations.reserve("s1", NOW.plus(Duration.ofHours(3)));
        clock.set(NOW.plus(Duration.ofHours(2).plusMinutes(31)));
        assertThrows(IllegalStateException.class, () -> f.reservations.confirm("s1"));
        assertFalse(f.reservations.find("s1").orElseThrow().confirmed());
    }

    @Test void confirmedReservationActivatesAtWallClockDeadline() {
        MutableClock clock = new MutableClock(NOW);
        Fixture f = fixtureWithClock(clock);
        Instant start = NOW.plus(Duration.ofHours(3));
        f.reservations.reserve("s1", start);
        f.reservations.confirm("s1");
        clock.set(start);
        f.reservations.activateDue("s1");
        assertEquals(SiegePhase.ACTIVE, f.siege.phase());
    }

    private static Fixture fixture() { return fixtureAt(NOW); }
    private static Fixture fixtureAt(Instant instant) { return fixtureWithClock(Clock.fixed(instant, ZoneOffset.UTC)); }
    private static Fixture fixtureWithClock(Clock clock) {
        GameState state = new GameState();
        state.addNation(new NationState("red", "적국")); state.addNation(new NationState("blue", "청국"));
        state.addStrategicPoint(new StrategicPoint("fort", "대요새", StrategicPointType.MAJOR, "blue",
                new PointPosition("world",0,64,0),1, Map.of()));
        SiegeService sieges = new SiegeService(state);
        SiegeInstance siege = sieges.create("s1","fort","red");
        SiegeReservationService.Policy provisional = new SiegeReservationService.Policy() {
            public Duration minimumLeadTime() { return Duration.ofHours(2); }
            public Duration confirmationDeadlineBeforeStart() { return Duration.ofMinutes(30); }
        };
        return new Fixture(siege, new SiegeReservationService(sieges, clock, provisional));
    }
    private record Fixture(SiegeInstance siege, SiegeReservationService reservations) {}

    private static final class MutableClock extends Clock {
        private Instant instant;
        MutableClock(Instant instant) { this.instant = instant; }
        void set(Instant instant) { this.instant = instant; }
        public ZoneId getZone() { return ZoneOffset.UTC; }
        public Clock withZone(ZoneId zone) { return this; }
        public Instant instant() { return instant; }
    }
}
