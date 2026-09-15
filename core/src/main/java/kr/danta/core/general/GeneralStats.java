package kr.danta.core.general;

/**
 * Four general stats fixed by design v0.3:
 * command(통솔), martial(무력), strategy(지략), logistics(병참).
 * Numeric ranges and effect coefficients remain balance data.
 */
public record GeneralStats(
        int command,
        int martial,
        int strategy,
        int logistics
) {
    public GeneralStats {
        requireNonNegative(command, "command");
        requireNonNegative(martial, "martial");
        requireNonNegative(strategy, "strategy");
        requireNonNegative(logistics, "logistics");
    }

    public static GeneralStats zero() {
        return new GeneralStats(0, 0, 0, 0);
    }

    private static void requireNonNegative(int value, String label) {
        if (value < 0) throw new IllegalArgumentException(label + " must be >= 0");
    }
}
