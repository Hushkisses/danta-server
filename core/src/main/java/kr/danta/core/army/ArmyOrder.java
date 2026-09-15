package kr.danta.core.army;

import java.util.Objects;

/** A validated army instruction waiting for later runtime scheduling. */
public record ArmyOrder(
        String orderId,
        String armyId,
        ArmyOrderType type,
        ArmyRoute route,
        ArmyOrderStatus status
) {
    public ArmyOrder {
        orderId = requireId(orderId, "orderId");
        armyId = requireId(armyId, "armyId");
        type = Objects.requireNonNull(type, "type");
        route = Objects.requireNonNull(route, "route");
        status = Objects.requireNonNull(status, "status");
    }

    private static String requireId(String value, String label) {
        Objects.requireNonNull(value, label);
        String normalized = value.trim();
        if (!normalized.matches("[A-Za-z0-9_-]{1,64}")) {
            throw new IllegalArgumentException(label + " must match [A-Za-z0-9_-]{1,64}");
        }
        return normalized;
    }
}
