package kr.danta.core.economy;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/** Nation-owned strategic-resource stockpile. Values are whole logical units. */
public final class StrategicResourceStockpile {
    private final String nationId;
    private final EnumMap<StrategicResource, Long> amounts = new EnumMap<>(StrategicResource.class);

    public StrategicResourceStockpile(String nationId) {
        this(nationId, Map.of());
    }

    public StrategicResourceStockpile(String nationId, Map<StrategicResource, Long> initial) {
        this.nationId = requireId(nationId);
        for (StrategicResource resource : StrategicResource.values()) amounts.put(resource, 0L);
        if (initial != null) initial.forEach(this::set);
    }

    public String nationId() { return nationId; }
    public synchronized long amount(StrategicResource resource) {
        return amounts.get(Objects.requireNonNull(resource, "resource"));
    }
    public synchronized Map<StrategicResource, Long> amounts() { return Map.copyOf(amounts); }

    public synchronized void set(StrategicResource resource, long amount) {
        Objects.requireNonNull(resource, "resource");
        if (amount < 0L) throw new IllegalArgumentException("amount must be >= 0");
        amounts.put(resource, amount);
    }
    public synchronized void deposit(StrategicResource resource, long amount) {
        Objects.requireNonNull(resource, "resource");
        if (amount <= 0L) throw new IllegalArgumentException("amount must be > 0");
        amounts.put(resource, Math.addExact(amounts.get(resource), amount));
    }
    public synchronized boolean tryConsume(StrategicResource resource, long amount) {
        Objects.requireNonNull(resource, "resource");
        if (amount <= 0L) throw new IllegalArgumentException("amount must be > 0");
        long current = amounts.get(resource);
        if (amount > current) return false;
        amounts.put(resource, current - amount);
        return true;
    }
    private static String requireId(String value) {
        Objects.requireNonNull(value, "nationId");
        String normalized = value.trim();
        if (normalized.isEmpty() || !normalized.matches("[A-Za-z0-9_-]{1,32}"))
            throw new IllegalArgumentException("invalid nationId");
        return normalized;
    }
}
