package kr.danta.paper.combat.live;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WaypointProgressTrackerTest {

    @Test
    void advancesToNextWaypointOnlyAfterCurrentOneIsReached() {
        WaypointProgressTracker tracker = new WaypointProgressTracker(1.25);
        UUID unitId = UUID.randomUUID();
        TacticalRoute route = new TacticalRoute("advance", List.of(
                new TacticalWaypoint("advance-1", -6.0, 64.0, 0.0),
                new TacticalWaypoint("advance-2", -1.5, 64.0, 0.0)
        ));

        assertEquals("advance-1", tracker.target(unitId, route, -12.0, 64.0, 0.0).orElseThrow().id());
        assertEquals("advance-2", tracker.target(unitId, route, -6.0, 64.0, 0.0).orElseThrow().id());
        assertEquals("advance-2", tracker.target(unitId, route, -3.0, 64.0, 0.0).orElseThrow().id());
        assertEquals(true, tracker.target(unitId, route, -1.5, 64.0, 0.0).isEmpty());
    }

    @Test
    void changingRouteResetsProgressToThatRoutesFirstWaypoint() {
        WaypointProgressTracker tracker = new WaypointProgressTracker(1.25);
        UUID unitId = UUID.randomUUID();
        TacticalRoute advance = new TacticalRoute("advance", List.of(
                new TacticalWaypoint("advance-1", -6.0, 64.0, 0.0),
                new TacticalWaypoint("advance-2", -1.5, 64.0, 0.0)
        ));
        TacticalRoute retreat = new TacticalRoute("retreat", List.of(
                new TacticalWaypoint("retreat-1", -14.0, 64.0, 0.0)
        ));

        tracker.target(unitId, advance, -6.0, 64.0, 0.0);
        assertEquals("retreat-1", tracker.target(unitId, retreat, -6.0, 64.0, 0.0).orElseThrow().id());
    }
}
