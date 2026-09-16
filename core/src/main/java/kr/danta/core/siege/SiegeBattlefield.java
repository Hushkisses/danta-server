package kr.danta.core.siege;

import java.util.List;
import java.util.Objects;

/** DEV-112 logical test battlefield for one major fortress. World construction remains separate. */
public record SiegeBattlefield(
        String battlefieldId,
        String strategicPointId,
        String worldName,
        int centerX,
        int centerY,
        int centerZ,
        int radius,
        List<SiegeObjective> objectives
) {
    public SiegeBattlefield {
        battlefieldId = require(battlefieldId, "battlefieldId");
        strategicPointId = require(strategicPointId, "strategicPointId");
        worldName = require(worldName, "worldName");
        if (radius <= 0) throw new IllegalArgumentException("radius must be > 0");
        objectives = objectives == null ? List.of() : List.copyOf(objectives);
    }

    public boolean contains(String world, double x, double z) {
        if (!worldName.equals(world)) return false;
        double dx = x - centerX, dz = z - centerZ;
        return dx * dx + dz * dz <= (double) radius * radius;
    }

    private static String require(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) throw new IllegalArgumentException(name + " must not be blank");
        return value;
    }
}
