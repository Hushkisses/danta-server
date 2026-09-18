package kr.danta.core.army;

import kr.danta.core.combat.TroopType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** DEV-030 army aggregate, extended by DEV-119 with authoritative troop-type composition. */
public final class ArmyState {
    private final String armyId;
    private final String ownerNationId;
    private String locationPointId;
    private ArmyStatus status;
    private long baseTroops;
    private final EnumMap<TroopType, Long> troopComposition = new EnumMap<>(TroopType.class);
    private ExpeditionSupplyLevel expeditionSupplyLevel;
    private long carriedFood;
    private String commanderGeneralId;

    public ArmyState(String armyId, String ownerNationId, String locationPointId,
                     ArmyStatus status, long baseTroops) {
        this.armyId = requireId(armyId, "armyId");
        this.ownerNationId = requireId(ownerNationId, "ownerNationId");
        this.locationPointId = requireId(locationPointId, "locationPointId");
        this.status = Objects.requireNonNull(status, "status");
        replaceTroopComposition(legacyComposition(baseTroops));
    }

    public ArmyState(String armyId, String ownerNationId, String locationPointId,
                     ArmyStatus status, long baseTroops, ExpeditionSupplyLevel expeditionSupplyLevel, long carriedFood) {
        this(armyId, ownerNationId, locationPointId, status, baseTroops);
        if (carriedFood < 0L) throw new IllegalArgumentException("carriedFood must be >= 0");
        this.expeditionSupplyLevel = expeditionSupplyLevel;
        this.carriedFood = carriedFood;
    }

    public ArmyState(String armyId, String ownerNationId, String locationPointId,
                     ArmyStatus status, Map<TroopType, Long> troopComposition) {
        this.armyId = requireId(armyId, "armyId");
        this.ownerNationId = requireId(ownerNationId, "ownerNationId");
        this.locationPointId = requireId(locationPointId, "locationPointId");
        this.status = Objects.requireNonNull(status, "status");
        replaceTroopComposition(troopComposition);
    }

    public ArmyState(String armyId, String ownerNationId, String locationPointId,
                     ArmyStatus status, Map<TroopType, Long> troopComposition,
                     ExpeditionSupplyLevel expeditionSupplyLevel, long carriedFood) {
        this(armyId, ownerNationId, locationPointId, status, troopComposition);
        if (carriedFood < 0L) throw new IllegalArgumentException("carriedFood must be >= 0");
        this.expeditionSupplyLevel = expeditionSupplyLevel;
        this.carriedFood = carriedFood;
    }

    public String armyId() { return armyId; }
    public String ownerNationId() { return ownerNationId; }
    public synchronized String locationPointId() { return locationPointId; }
    public synchronized ArmyStatus status() { return status; }
    public synchronized long baseTroops() { return baseTroops; }
    public synchronized long totalTroops() { return baseTroops; }
    public synchronized long troopCount(TroopType type) {
        return troopComposition.getOrDefault(Objects.requireNonNull(type, "type"), 0L);
    }
    public synchronized Map<TroopType, Long> troopComposition() {
        return Collections.unmodifiableMap(new EnumMap<>(troopComposition));
    }
    public synchronized ExpeditionSupplyLevel expeditionSupplyLevel() { return expeditionSupplyLevel; }
    public synchronized long carriedFood() { return carriedFood; }
    public synchronized Optional<String> commanderGeneralId() { return Optional.ofNullable(commanderGeneralId); }

    public synchronized void setCommanderGeneralId(String generalId) {
        this.commanderGeneralId = requireId(generalId, "generalId");
    }

    public synchronized void clearCommanderGeneralId() {
        this.commanderGeneralId = null;
    }

    public synchronized void setExpeditionSupply(ExpeditionSupplyLevel level, long carriedFood) {
        this.expeditionSupplyLevel = Objects.requireNonNull(level, "level");
        if (carriedFood < 0L) throw new IllegalArgumentException("carriedFood must be >= 0");
        this.carriedFood = carriedFood;
    }

    public synchronized void setLocationPointId(String locationPointId) {
        this.locationPointId = requireId(locationPointId, "locationPointId");
    }

    public synchronized void setStatus(ArmyStatus status) {
        this.status = Objects.requireNonNull(status, "status");
    }

    /**
     * Legacy total-only mutation. Until all callers become composition-aware, this resets composition
     * to infantry-only so baseTroops never becomes a second source of truth.
     */
    public synchronized void setBaseTroops(long baseTroops) {
        replaceTroopComposition(legacyComposition(baseTroops));
    }

    public synchronized void replaceTroopComposition(Map<TroopType, Long> replacement) {
        Objects.requireNonNull(replacement, "replacement");
        EnumMap<TroopType, Long> validated = new EnumMap<>(TroopType.class);
        long total = 0L;

        for (TroopType type : TroopType.values()) {
            long count = replacement.getOrDefault(type, 0L);
            if (count < 0L) throw new IllegalArgumentException("troop count must be >= 0: " + type);
            total = Math.addExact(total, count);
            validated.put(type, count);
        }

        for (Map.Entry<TroopType, Long> entry : replacement.entrySet()) {
            if (entry.getKey() == null) throw new NullPointerException("troop type");
            if (entry.getValue() == null) throw new NullPointerException("troop count");
        }

        troopComposition.clear();
        troopComposition.putAll(validated);
        baseTroops = total;
    }

    private static Map<TroopType, Long> legacyComposition(long baseTroops) {
        if (baseTroops < 0L) throw new IllegalArgumentException("baseTroops must be >= 0");
        return Map.of(TroopType.INFANTRY, baseTroops);
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
