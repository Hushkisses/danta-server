package kr.danta.paper.combat.live;

public record TacticalWaypoint(String id, double x, double y, double z) {
    public TacticalWaypoint {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("waypoint id is blank");
        }
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
            throw new IllegalArgumentException("waypoint coordinates must be finite");
        }
    }
}
