package kr.danta.paper.combat.live;

import java.util.List;

public record TacticalRoute(String id, List<TacticalWaypoint> waypoints) {
    public TacticalRoute {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("route id is blank");
        }
        waypoints = List.copyOf(waypoints);
        if (waypoints.isEmpty()) {
            throw new IllegalArgumentException("route requires at least one waypoint");
        }
    }
}
