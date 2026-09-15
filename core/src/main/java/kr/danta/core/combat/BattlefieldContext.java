package kr.danta.core.combat;

import java.util.Objects;

/**
 * DEV-063 explicit battlefield context.
 *
 * Capacity is a positive combat-space limit. DEV-063 exposes capacity pressure
 * without inventing an automatic damage coefficient for over-capacity troops.
 */
public record BattlefieldContext(
        BattlefieldTerrain terrain,
        int capacity
) {
    public BattlefieldContext {
        Objects.requireNonNull(terrain, "terrain");
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be positive");
    }

    public static BattlefieldContext unconstrained(BattlefieldTerrain terrain) {
        return new BattlefieldContext(terrain, Integer.MAX_VALUE);
    }
}
