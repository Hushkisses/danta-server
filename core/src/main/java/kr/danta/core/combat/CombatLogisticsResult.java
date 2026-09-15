package kr.danta.core.combat;

import java.util.Objects;

public record CombatLogisticsResult(
        CombatLogisticsSideResult first,
        CombatLogisticsSideResult second
) {
    public CombatLogisticsResult {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
    }
}
