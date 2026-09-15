package kr.danta.core.army;

import java.util.List;
import java.util.Objects;

/** DEV-034 ordered destination queue. The army's current location is implicit. */
public record ArmyOperationQueue(String armyId, List<String> destinations) {
    public ArmyOperationQueue {
        armyId = requireId(armyId, "armyId");
        destinations = destinations == null ? List.of() : destinations.stream()
                .map(value -> requireId(value, "destinationPointId")).toList();
        if (destinations.isEmpty()) throw new IllegalArgumentException("operation queue must contain at least one destination");
    }

    public String nextDestination() { return destinations.getFirst(); }
    public boolean hasFollowingLeg() { return destinations.size() > 1; }
    public ArmyOperationQueue afterArrival() {
        if (!hasFollowingLeg()) throw new IllegalStateException("operation queue has no following leg");
        return new ArmyOperationQueue(armyId, destinations.subList(1, destinations.size()));
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
