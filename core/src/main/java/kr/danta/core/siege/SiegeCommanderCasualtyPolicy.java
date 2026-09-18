package kr.danta.core.siege;

/** DEV-118 provisional commander-loss penalty. Numeric value is development-only. */
public final class SiegeCommanderCasualtyPolicy {
    public static final int DEVELOPMENT_MORALE_PENALTY = 20;

    private final int moralePenalty;

    public SiegeCommanderCasualtyPolicy(int moralePenalty) {
        if (moralePenalty < 0) throw new IllegalArgumentException("moralePenalty must be >= 0");
        this.moralePenalty = moralePenalty;
    }

    public static SiegeCommanderCasualtyPolicy developmentDefaults() {
        return new SiegeCommanderCasualtyPolicy(DEVELOPMENT_MORALE_PENALTY);
    }

    public int moralePenalty() {
        return moralePenalty;
    }

    public SiegeCommanderCasualty casualty(String pointId, java.util.UUID playerId, SiegeSide side) {
        return new SiegeCommanderCasualty(pointId, playerId, side, -moralePenalty, false);
    }
}
