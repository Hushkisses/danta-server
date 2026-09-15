package kr.danta.core.combat;

import java.util.Objects;

/**
 * DEV-041 deterministic combat-power resolver v0.
 * Formula: troops * troop coefficient * commander * research * terrain * supply * morale * counter.
 */
public final class CombatResolver {
    private static final double DRAW_EPSILON = 1.0e-9;

    public CombatResult resolve(CombatSideInput first, CombatSideInput second) {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
        if (first.sideId().equals(second.sideId())) throw new IllegalArgumentException("combat sides must be different");

        CombatSideResult firstResult = evaluate(first, second.troopType());
        CombatSideResult secondResult = evaluate(second, first.troopType());
        double difference = firstResult.effectivePower() - secondResult.effectivePower();
        String winner = Math.abs(difference) <= DRAW_EPSILON ? null
                : difference > 0.0 ? first.sideId() : second.sideId();
        return new CombatResult(firstResult, secondResult, winner);
    }

    private CombatSideResult evaluate(CombatSideInput side, TroopType opponentType) {
        TroopTypeDefinition definition = BasicTroopTypes.definition(side.troopType());
        double counter = definition.strongAgainst() == opponentType ? definition.strongAgainstMultiplier() : 1.0;
        double power = side.troopCount()
                * definition.basePower()
                * side.commanderMultiplier()
                * side.researchMultiplier()
                * side.terrainMultiplier()
                * side.supplyMultiplier()
                * side.moraleMultiplier()
                * counter;
        return new CombatSideResult(side.sideId(), side.troopType(), side.troopCount(),
                definition.basePower(), counter, power);
    }
}
