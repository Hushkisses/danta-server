package kr.danta.core.snapshot;

import kr.danta.core.army.ArmyStatus;
import kr.danta.core.army.ExpeditionSupplyLevel;
import kr.danta.core.combat.TroopType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/** Persisted army state. DEV-119 schema v21 adds authoritative troop-type composition. */
public record ArmySnapshot(
        String armyId,
        String ownerNationId,
        String locationPointId,
        ArmyStatus status,
        long baseTroops,
        ExpeditionSupplyLevel expeditionSupplyLevel,
        long carriedFood,
        Map<TroopType, Long> troopComposition
) {
    public ArmySnapshot {
        Objects.requireNonNull(armyId, "armyId");
        Objects.requireNonNull(ownerNationId, "ownerNationId");
        Objects.requireNonNull(locationPointId, "locationPointId");
        Objects.requireNonNull(status, "status");
        if (baseTroops < 0L) throw new IllegalArgumentException("baseTroops must be >= 0");
        if (carriedFood < 0L) throw new IllegalArgumentException("carriedFood must be >= 0");

        EnumMap<TroopType, Long> normalized = normalize(troopComposition);
        long total = 0L;
        for (long count : normalized.values()) total = Math.addExact(total, count);
        if (total != baseTroops) {
            throw new IllegalArgumentException("baseTroops must equal troop composition total");
        }
        troopComposition = Collections.unmodifiableMap(normalized);
    }

    public ArmySnapshot(String armyId, String ownerNationId, String locationPointId,
                        ArmyStatus status, long baseTroops) {
        this(armyId, ownerNationId, locationPointId, status, baseTroops, null, 0L,
                legacyComposition(baseTroops));
    }

    public ArmySnapshot(String armyId, String ownerNationId, String locationPointId,
                        ArmyStatus status, long baseTroops,
                        ExpeditionSupplyLevel expeditionSupplyLevel, long carriedFood) {
        this(armyId, ownerNationId, locationPointId, status, baseTroops,
                expeditionSupplyLevel, carriedFood, legacyComposition(baseTroops));
    }

    private static EnumMap<TroopType, Long> normalize(Map<TroopType, Long> source) {
        Objects.requireNonNull(source, "troopComposition");
        EnumMap<TroopType, Long> result = new EnumMap<>(TroopType.class);
        for (TroopType type : TroopType.values()) {
            long count = source.getOrDefault(type, 0L);
            if (count < 0L) throw new IllegalArgumentException("troop count must be >= 0: " + type);
            result.put(type, count);
        }
        for (Map.Entry<TroopType, Long> entry : source.entrySet()) {
            if (entry.getKey() == null) throw new NullPointerException("troop type");
            if (entry.getValue() == null) throw new NullPointerException("troop count");
        }
        return result;
    }

    private static Map<TroopType, Long> legacyComposition(long baseTroops) {
        if (baseTroops < 0L) throw new IllegalArgumentException("baseTroops must be >= 0");
        return Map.of(TroopType.INFANTRY, baseTroops);
    }
}
