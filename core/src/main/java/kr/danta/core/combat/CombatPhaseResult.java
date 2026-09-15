package kr.danta.core.combat;

import java.util.Objects;

/** Immutable diagnostic result for one logical phase of CombatResolver v1. */
public record CombatPhaseResult(
        CombatPhase phase,
        double firstPower,
        double secondPower,
        double firstMultiplier,
        double secondMultiplier
) {
    public CombatPhaseResult {
        Objects.requireNonNull(phase, "phase");
        validate(firstPower, "firstPower");
        validate(secondPower, "secondPower");
        validateMultiplier(firstMultiplier, "firstMultiplier");
        validateMultiplier(secondMultiplier, "secondMultiplier");
    }

    public static CombatPhaseResult neutral(CombatPhase phase, double firstPower, double secondPower) {
        return new CombatPhaseResult(phase, firstPower, secondPower, 1.0, 1.0);
    }

    private static void validate(double value, String label) {
        if (!Double.isFinite(value) || value < 0.0) throw new IllegalArgumentException(label + " must be finite and non-negative");
    }

    private static void validateMultiplier(double value, String label) {
        if (!Double.isFinite(value) || value <= 0.0) throw new IllegalArgumentException(label + " must be positive");
    }
}
