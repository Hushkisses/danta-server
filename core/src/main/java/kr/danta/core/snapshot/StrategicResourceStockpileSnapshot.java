package kr.danta.core.snapshot;

import kr.danta.core.economy.StrategicResource;
import java.util.Map;

public record StrategicResourceStockpileSnapshot(String nationId, Map<StrategicResource, Long> amounts) {
    public StrategicResourceStockpileSnapshot {
        if (nationId == null || nationId.isBlank()) throw new IllegalArgumentException("nationId must not be blank");
        amounts = amounts == null ? Map.of() : Map.copyOf(amounts);
        for (long value : amounts.values()) if (value < 0L) throw new IllegalArgumentException("resource amount must be >= 0");
    }
}
