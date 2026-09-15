package kr.danta.core.general;

/**
 * DEV-071 four general stats defined by design v0.3.
 *
 * The design fixes the axes (command/martial/intelligence/politics) but does not
 * yet fix an authoritative numeric range or grade/level growth table. Values are
 * therefore non-negative raw stats; combat/economy coefficients are deferred.
 */
public record GeneralStats(
        int command,
        int martial,
        int intelligence,
        int politics
) {
    public GeneralStats {
        requireNonNegative(command, "command");
        requireNonNegative(martial, "martial");
        requireNonNegative(intelligence, "intelligence");
        requireNonNegative(politics, "politics");
    }

    public static GeneralStats zero() {
        return new GeneralStats(0, 0, 0, 0);
    }

    private static void requireNonNegative(int value, String label) {
        if (value < 0) throw new IllegalArgumentException(label + " must be >= 0");
    }
}
