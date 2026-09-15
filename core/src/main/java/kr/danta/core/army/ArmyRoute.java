package kr.danta.core.army;

import java.util.Objects;

/** One adjacent strategic-edge leg. Multi-leg routes are introduced in DEV-034. */
public record ArmyRoute(String originPointId, String destinationPointId, String edgeId) {
    public ArmyRoute {
        originPointId = requireId(originPointId, "originPointId");
        destinationPointId = requireId(destinationPointId, "destinationPointId");
        edgeId = requireId(edgeId, "edgeId");
        if (originPointId.equals(destinationPointId)) {
            throw new IllegalArgumentException("route endpoints must be different");
        }
    }

    private static String requireId(String value, String label) {
        Objects.requireNonNull(value, label);
        String normalized = value.trim();
        if (!normalized.matches("[A-Za-z0-9_-]{1,48}")) {
            throw new IllegalArgumentException(label + " must match [A-Za-z0-9_-]{1,48}");
        }
        return normalized;
    }
}
