package kr.danta.core.combat;

import java.util.Objects;

public record CombatScenario(
        String id,
        CombatSideInput first,
        CombatSideInput second,
        String expectedWinnerSideId,
        double expectedFirstPower,
        double expectedSecondPower
) {
    public CombatScenario {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
        if (id.isBlank()) throw new IllegalArgumentException("id must not be blank");
    }

    public boolean expectsDraw() {
        return expectedWinnerSideId == null;
    }
}
