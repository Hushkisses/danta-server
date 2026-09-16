package kr.danta.paper.world;

import java.util.Objects;

/**
 * DEV-MAP-003 stable development defaults for the strategic mainland and wilderness worlds.
 *
 * <p>Coordinates are relative to each world's persisted Bukkit spawn so they remain stable across
 * restarts without hard-coding terrain height. Border size/reset cadence intentionally do not live
 * here until their balance is decided.</p>
 */
public record DantaWorldConfig(
        String strategicWorldName,
        String wildernessWorldName,
        RelativePoint strategicGateOffset,
        RelativePoint strategicArrivalOffset,
        RelativePoint wildernessGateOffset,
        RelativePoint wildernessArrivalOffset
) {
    public DantaWorldConfig {
        strategicWorldName = requireName(strategicWorldName, "strategicWorldName");
        wildernessWorldName = requireName(wildernessWorldName, "wildernessWorldName");
        if (strategicWorldName.equals(wildernessWorldName)) {
            throw new IllegalArgumentException("strategic and wilderness world names must differ");
        }
        Objects.requireNonNull(strategicGateOffset, "strategicGateOffset");
        Objects.requireNonNull(strategicArrivalOffset, "strategicArrivalOffset");
        Objects.requireNonNull(wildernessGateOffset, "wildernessGateOffset");
        Objects.requireNonNull(wildernessArrivalOffset, "wildernessArrivalOffset");
    }

    public static DantaWorldConfig defaults() {
        return new DantaWorldConfig(
                "danta_main",
                "danta_wild",
                new RelativePoint(4, 0, 0),
                new RelativePoint(0, 0, 0),
                new RelativePoint(0, 0, 0),
                new RelativePoint(4, 0, 0)
        );
    }

    public RelativePoint gateOffset(TravelGate gate) {
        return switch (gate) {
            case EXPLORERS_GUILD -> strategicGateOffset;
            case WILDERNESS_RETURN -> wildernessGateOffset;
        };
    }

    public RelativePoint arrivalOffset(WorldRole role) {
        return switch (role) {
            case STRATEGIC_MAIN -> strategicArrivalOffset;
            case WILDERNESS -> wildernessArrivalOffset;
        };
    }

    private static String requireName(String value, String field) {
        Objects.requireNonNull(value, field);
        String normalized = value.trim();
        if (normalized.isEmpty()) throw new IllegalArgumentException(field + " must not be blank");
        return normalized;
    }

    public record RelativePoint(int x, int y, int z) {}
}
