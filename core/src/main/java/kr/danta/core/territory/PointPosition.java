package kr.danta.core.territory;

import java.util.Objects;

/** Logical anchor of a strategic point in the Minecraft world. */
public record PointPosition(String worldName, int x, int y, int z) {
    public PointPosition {
        Objects.requireNonNull(worldName, "worldName");
        worldName = worldName.trim();
        if (worldName.isEmpty()) throw new IllegalArgumentException("worldName must not be blank");
        if (worldName.length() > 64) throw new IllegalArgumentException("worldName must be <= 64 chars");
    }
}
