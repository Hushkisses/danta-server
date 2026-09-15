package kr.danta.core.combat;

import java.util.Objects;

/**
 * DEV-062 explicit morale/retreat/pursuit inputs.
 * Route blocking/isolation and their additional effects belong to DEV-064.
 */
public record RetreatPursuitContext(
        MoraleState firstMorale,
        MoraleState secondMorale
) {
    public RetreatPursuitContext {
        Objects.requireNonNull(firstMorale, "firstMorale");
        Objects.requireNonNull(secondMorale, "secondMorale");
    }

    public static RetreatPursuitContext normal() {
        return new RetreatPursuitContext(MoraleState.NORMAL, MoraleState.NORMAL);
    }
}
