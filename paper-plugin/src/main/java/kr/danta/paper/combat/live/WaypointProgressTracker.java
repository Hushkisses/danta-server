package kr.danta.paper.combat.live;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Maintains per-unit progress through the currently selected tactical route. */
public final class WaypointProgressTracker {
    private final double reachedDistanceSquared;
    private final Map<UUID, Progress> progressByUnit = new HashMap<>();

    public WaypointProgressTracker(double reachedDistance) {
        if (reachedDistance <= 0.0) throw new IllegalArgumentException("reachedDistance must be > 0");
        this.reachedDistanceSquared = reachedDistance * reachedDistance;
    }

    public Optional<TacticalWaypoint> target(
            UUID unitId,
            TacticalRoute route,
            double x,
            double y,
            double z
    ) {
        if (unitId == null) throw new NullPointerException("unitId");
        if (route == null) throw new NullPointerException("route");
        if (route.waypoints().isEmpty()) {
            progressByUnit.remove(unitId);
            return Optional.empty();
        }

        Progress progress = progressByUnit.get(unitId);
        if (progress == null || !progress.routeId().equals(route.id())) {
            progress = new Progress(route.id(), 0);
        }

        int index = Math.min(progress.index(), route.waypoints().size() - 1);
        TacticalWaypoint waypoint = route.waypoints().get(index);

        while (distanceSquared(x, y, z, waypoint) <= reachedDistanceSquared) {
            index++;
            if (index >= route.waypoints().size()) {
                progressByUnit.put(unitId, new Progress(route.id(), route.waypoints().size()));
                return Optional.empty();
            }
            waypoint = route.waypoints().get(index);
        }

        progressByUnit.put(unitId, new Progress(route.id(), index));
        return Optional.of(waypoint);
    }

    public void clear(UUID unitId) {
        if (unitId != null) progressByUnit.remove(unitId);
    }

    public void clear() {
        progressByUnit.clear();
    }

    private static double distanceSquared(double x, double y, double z, TacticalWaypoint waypoint) {
        double dx = waypoint.x() - x;
        double dy = waypoint.y() - y;
        double dz = waypoint.z() - z;
        return dx * dx + dy * dy + dz * dz;
    }

    private record Progress(String routeId, int index) {}
}
