package kr.danta.core.snapshot;

import kr.danta.core.territory.StrategicPointType;

import java.util.Map;

/** Persisted DEV-021 strategic point state. */
public record StrategicPointSnapshot(
        String pointId,
        String displayName,
        StrategicPointType type,
        String ownerNationId,
        String worldName,
        int x,
        int y,
        int z,
        int facilitySlots,
        Map<String, Long> baseProductionPerHour
) {
    public StrategicPointSnapshot {
        baseProductionPerHour = baseProductionPerHour == null ? Map.of() : Map.copyOf(baseProductionPerHour);
    }
}
