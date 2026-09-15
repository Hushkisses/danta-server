package kr.danta.core.army;

import java.util.Objects;

/** DEV-030 army aggregate. Troop composition is intentionally deferred to DEV-040. */
public final class ArmyState {
    private final String armyId;
    private final String ownerNationId;
    private String locationPointId;
    private ArmyStatus status;
    private long baseTroops;
    private ExpeditionSupplyLevel expeditionSupplyLevel;
    private long carriedFood;

    public ArmyState(String armyId, String ownerNationId, String locationPointId,
                     ArmyStatus status, long baseTroops) {
        this.armyId = requireId(armyId, "armyId");
        this.ownerNationId = requireId(ownerNationId, "ownerNationId");
        this.locationPointId = requireId(locationPointId, "locationPointId");
        this.status = Objects.requireNonNull(status, "status");
        setBaseTroops(baseTroops);
    }

    public ArmyState(String armyId, String ownerNationId, String locationPointId,
                     ArmyStatus status, long baseTroops, ExpeditionSupplyLevel expeditionSupplyLevel, long carriedFood) {
        this(armyId, ownerNationId, locationPointId, status, baseTroops);
        if (carriedFood < 0L) throw new IllegalArgumentException("carriedFood must be >= 0");
        this.expeditionSupplyLevel = expeditionSupplyLevel;
        this.carriedFood = carriedFood;
    }

    public String armyId() { return armyId; }
    public String ownerNationId() { return ownerNationId; }
    public synchronized String locationPointId() { return locationPointId; }
    public synchronized ArmyStatus status() { return status; }
    public synchronized long baseTroops() { return baseTroops; }
    public synchronized ExpeditionSupplyLevel expeditionSupplyLevel() { return expeditionSupplyLevel; }
    public synchronized long carriedFood() { return carriedFood; }

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

    public synchronized void setBaseTroops(long baseTroops) {
        if (baseTroops < 0L) throw new IllegalArgumentException("baseTroops must be >= 0");
        this.baseTroops = baseTroops;
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
