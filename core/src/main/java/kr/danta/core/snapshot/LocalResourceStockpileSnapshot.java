package kr.danta.core.snapshot;

import kr.danta.core.economy.StrategicResource;
import java.util.Map;

public record LocalResourceStockpileSnapshot(String pointId, Map<StrategicResource, Long> amounts) {
    public LocalResourceStockpileSnapshot {
        if (pointId == null || pointId.isBlank()) throw new IllegalArgumentException("pointId must not be blank");
        amounts = amounts == null ? Map.of() : Map.copyOf(amounts);
        for (long v : amounts.values()) if (v < 0) throw new IllegalArgumentException("amount must be >= 0");
    }
}
