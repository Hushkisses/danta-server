package kr.danta.core.economy;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/** DEV-054 resources physically/logically retained at an isolated strategic point. */
public final class LocalResourceStockpile {
    private final String pointId;
    private final EnumMap<StrategicResource, Long> amounts = new EnumMap<>(StrategicResource.class);

    public LocalResourceStockpile(String pointId) { this(pointId, Map.of()); }
    public LocalResourceStockpile(String pointId, Map<StrategicResource, Long> initial) {
        this.pointId = Objects.requireNonNull(pointId, "pointId").trim();
        if (this.pointId.isEmpty()) throw new IllegalArgumentException("pointId must not be blank");
        for (StrategicResource r : StrategicResource.values()) amounts.put(r, 0L);
        if (initial != null) initial.forEach(this::set);
    }
    public String pointId() { return pointId; }
    public synchronized long amount(StrategicResource r) { return amounts.get(Objects.requireNonNull(r)); }
    public synchronized Map<StrategicResource, Long> amounts() { return Map.copyOf(amounts); }
    public synchronized void set(StrategicResource r, long amount) {
        if (amount < 0) throw new IllegalArgumentException("amount must be >= 0");
        amounts.put(Objects.requireNonNull(r), amount);
    }
    public synchronized void deposit(StrategicResource r, long amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be > 0");
        r = Objects.requireNonNull(r);
        amounts.put(r, Math.addExact(amounts.get(r), amount));
    }
}
