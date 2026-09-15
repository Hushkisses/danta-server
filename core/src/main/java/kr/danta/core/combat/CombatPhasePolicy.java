package kr.danta.core.combat;

/**
 * DEV-061 phase ownership policy for the four basic troop roles.
 *
 * The existing DEV-040 soft-counter multiplier remains the only numeric counter
 * bonus. DEV-061 moves that bonus into its logical battle phase instead of
 * inventing additional balance coefficients.
 */
public final class CombatPhasePolicy {
    private CombatPhasePolicy() {}

    public static CombatPhase phaseFor(TroopType type) {
        return switch (BasicTroopTypes.definition(type).role()) {
            case FRONTLINE -> CombatPhase.FRONTLINE;
            case RANGED -> CombatPhase.BACKLINE;
            case MOBILE -> CombatPhase.MOBILE;
        };
    }

    public static double counterMultiplier(TroopType ownType, TroopType opponentType) {
        TroopTypeDefinition definition = BasicTroopTypes.definition(ownType);
        return definition.strongAgainst() == opponentType ? definition.strongAgainstMultiplier() : 1.0;
    }
}
