package kr.danta.core.territory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * DEV-021 strategic point aggregate.
 * Production values are whole logical units per runtime hour and use string keys for now;
 * the economy resource enum is introduced later in DEV-051.
 */
public final class StrategicPoint {
    private final String pointId;
    private String displayName;
    private StrategicPointType type;
    private String ownerNationId;
    private PointPosition position;
    private int facilitySlots;
    private final Map<String, Long> baseProductionPerHour = new LinkedHashMap<>();

    public StrategicPoint(String pointId, String displayName, StrategicPointType type,
                          String ownerNationId, PointPosition position, int facilitySlots,
                          Map<String, Long> baseProductionPerHour) {
        this.pointId = requireId(pointId, "pointId");
        this.displayName = requireName(displayName);
        this.type = Objects.requireNonNull(type, "type");
        this.ownerNationId = normalizeOptionalId(ownerNationId);
        this.position = Objects.requireNonNull(position, "position");
        setFacilitySlots(facilitySlots);
        setBaseProduction(baseProductionPerHour);
    }

    public StrategicPoint(String pointId, String displayName, StrategicPointType type,
                          PointPosition position, int facilitySlots) {
        this(pointId, displayName, type, null, position, facilitySlots, Map.of());
    }

    public String pointId() { return pointId; }
    public synchronized String displayName() { return displayName; }
    public synchronized StrategicPointType type() { return type; }
    public synchronized Optional<String> ownerNationId() { return Optional.ofNullable(ownerNationId); }
    public synchronized PointPosition position() { return position; }
    public synchronized int facilitySlots() { return facilitySlots; }
    public synchronized Map<String, Long> baseProductionPerHour() { return Map.copyOf(baseProductionPerHour); }

    public synchronized void rename(String displayName) { this.displayName = requireName(displayName); }
    public synchronized void setType(StrategicPointType type) { this.type = Objects.requireNonNull(type, "type"); }
    public synchronized void setOwnerNationId(String ownerNationId) { this.ownerNationId = normalizeOptionalId(ownerNationId); }
    public synchronized void setPosition(PointPosition position) { this.position = Objects.requireNonNull(position, "position"); }

    public synchronized void setFacilitySlots(int facilitySlots) {
        if (facilitySlots < 0 || facilitySlots > 16) {
            throw new IllegalArgumentException("facilitySlots must be between 0 and 16");
        }
        this.facilitySlots = facilitySlots;
    }

    public synchronized void setBaseProduction(Map<String, Long> production) {
        baseProductionPerHour.clear();
        if (production == null) return;
        production.forEach(this::setBaseProduction);
    }

    public synchronized void setBaseProduction(String resourceKey, long amountPerHour) {
        String key = requireResourceKey(resourceKey);
        if (amountPerHour < 0L) throw new IllegalArgumentException("production must be >= 0");
        if (amountPerHour == 0L) baseProductionPerHour.remove(key);
        else baseProductionPerHour.put(key, amountPerHour);
    }

    private static String requireId(String value, String label) {
        Objects.requireNonNull(value, label);
        String normalized = value.trim();
        if (!normalized.matches("[A-Za-z0-9_-]{1,48}")) {
            throw new IllegalArgumentException(label + " must match [A-Za-z0-9_-]{1,48}");
        }
        return normalized;
    }

    private static String requireName(String value) {
        Objects.requireNonNull(value, "displayName");
        String normalized = value.trim();
        if (normalized.isEmpty()) throw new IllegalArgumentException("displayName must not be blank");
        if (normalized.length() > 64) throw new IllegalArgumentException("displayName must be <= 64 chars");
        return normalized;
    }

    private static String normalizeOptionalId(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        if (normalized.isEmpty() || normalized.equalsIgnoreCase("none")) return null;
        return requireId(normalized, "ownerNationId");
    }

    private static String requireResourceKey(String value) {
        Objects.requireNonNull(value, "resourceKey");
        String normalized = value.trim().toLowerCase();
        if (!normalized.matches("[a-z0-9_.-]{1,32}")) {
            throw new IllegalArgumentException("resourceKey must match [a-z0-9_.-]{1,32}");
        }
        return normalized;
    }
}
