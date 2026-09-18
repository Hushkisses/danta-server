package kr.danta.core.combat;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public record LogicalArmyCasualtyMergeResult(
        Map<TroopType, Long> before,
        Map<TroopType, Long> after,
        Map<TroopType, Long> losses,
        long totalLoss
) {
    public LogicalArmyCasualtyMergeResult {
        before = immutableEnumMap(before, "before");
        after = immutableEnumMap(after, "after");
        losses = immutableEnumMap(losses, "losses");
        if (totalLoss < 0L) throw new IllegalArgumentException("totalLoss must be >= 0");
    }

    private static Map<TroopType, Long> immutableEnumMap(Map<TroopType, Long> source, String name) {
        Objects.requireNonNull(source, name);
        return Collections.unmodifiableMap(new EnumMap<>(source));
    }
}
