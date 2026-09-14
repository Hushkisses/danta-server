package kr.danta.core.snapshot;

import java.util.List;

/** Restart-recovery snapshot. Strategic edges: schema v4 (DEV-022). */
public record GameSnapshot(
        int schemaVersion,
        long createdAtEpochMillis,
        long runtimeElapsedMillis,
        boolean runtimePaused,
        double runtimeSpeedMultiplier,
        String seasonId,
        String seasonDisplayName,
        List<NationSnapshot> nations,
        List<StrategicPointSnapshot> strategicPoints,
        List<StrategicEdgeSnapshot> strategicEdges
) {
    public static final int CURRENT_SCHEMA = 4;

    public GameSnapshot {
        if (schemaVersion <= 0) throw new IllegalArgumentException("schemaVersion must be positive");
        if (createdAtEpochMillis < 0 || runtimeElapsedMillis < 0) throw new IllegalArgumentException("negative time");
        if (!Double.isFinite(runtimeSpeedMultiplier) || runtimeSpeedMultiplier <= 0.0) {
            throw new IllegalArgumentException("invalid runtimeSpeedMultiplier");
        }
        if ((seasonId == null) != (seasonDisplayName == null)) {
            throw new IllegalArgumentException("season fields must both be null or non-null");
        }
        nations = nations == null ? List.of() : List.copyOf(nations);
        strategicPoints = strategicPoints == null ? List.of() : List.copyOf(strategicPoints);
        strategicEdges = strategicEdges == null ? List.of() : List.copyOf(strategicEdges);
    }
}
