package kr.danta.core.combat;

import java.util.Objects;

public record RetreatPursuitResult(
        String sideId,
        MoraleState morale,
        boolean routed,
        boolean pursuitAdvantageAgainstOpponent
) {
    public RetreatPursuitResult {
        Objects.requireNonNull(sideId, "sideId");
        Objects.requireNonNull(morale, "morale");
        if (sideId.isBlank()) throw new IllegalArgumentException("sideId must not be blank");
    }
}
