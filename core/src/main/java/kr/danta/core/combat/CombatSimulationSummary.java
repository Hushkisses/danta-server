package kr.danta.core.combat;

import java.util.Objects;

/** DEV-065 aggregate result for repeated deterministic combat runs. */
public record CombatSimulationSummary(
        String scenarioId,
        int runs,
        int firstWins,
        int secondWins,
        int draws,
        double firstWinRate,
        double secondWinRate,
        double drawRate,
        double averageFirstLosses,
        double averageSecondLosses
) {
    public CombatSimulationSummary {
        Objects.requireNonNull(scenarioId, "scenarioId");
        if (scenarioId.isBlank()) throw new IllegalArgumentException("scenarioId must not be blank");
        if (runs <= 0) throw new IllegalArgumentException("runs must be positive");
        if (firstWins < 0 || secondWins < 0 || draws < 0 || firstWins + secondWins + draws != runs)
            throw new IllegalArgumentException("outcome counts must sum to runs");
    }
}
