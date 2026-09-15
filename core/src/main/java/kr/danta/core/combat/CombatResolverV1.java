package kr.danta.core.combat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * DEV-060/061 staged combat resolver.
 *
 * DEV-061 assigns the existing DEV-040 soft counter to the logical phase owned
 * by each troop role: frontline, backline or mobile. Opening and the later
 * morale/retreat phases remain neutral until their dedicated tickets.
 */
public final class CombatResolverV1 {
    private static final double DRAW_EPSILON = 1.0e-9;

    private final CombatResolver baselineResolver;

    public CombatResolverV1() {
        this(new CombatResolver());
    }

    CombatResolverV1(CombatResolver baselineResolver) {
        this.baselineResolver = Objects.requireNonNull(baselineResolver, "baselineResolver");
    }

    public CombatV1Result resolve(CombatSideInput first, CombatSideInput second) {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");

        // Reuse DEV-041 for validated input semantics and the non-counter base formula.
        CombatResult baseline = baselineResolver.resolve(first, second);
        double firstCounter = CombatPhasePolicy.counterMultiplier(first.troopType(), second.troopType());
        double secondCounter = CombatPhasePolicy.counterMultiplier(second.troopType(), first.troopType());

        double firstPower = removeCounter(baseline.first().effectivePower(), baseline.first().counterMultiplier());
        double secondPower = removeCounter(baseline.second().effectivePower(), baseline.second().counterMultiplier());

        List<CombatPhaseResult> phases = new ArrayList<>(CombatPhase.values().length);
        for (CombatPhase phase : CombatPhase.values()) {
            double firstMultiplier = CombatPhasePolicy.phaseFor(first.troopType()) == phase ? firstCounter : 1.0;
            double secondMultiplier = CombatPhasePolicy.phaseFor(second.troopType()) == phase ? secondCounter : 1.0;
            firstPower *= firstMultiplier;
            secondPower *= secondMultiplier;
            phases.add(new CombatPhaseResult(phase, firstPower, secondPower, firstMultiplier, secondMultiplier));
        }

        CombatResult finalResult = withFinalPower(baseline, firstPower, secondPower);
        return new CombatV1Result(finalResult, phases);
    }

    private double removeCounter(double power, double counter) {
        return power / counter;
    }

    private CombatResult withFinalPower(CombatResult baseline, double firstPower, double secondPower) {
        CombatSideResult first = withPower(baseline.first(), firstPower);
        CombatSideResult second = withPower(baseline.second(), secondPower);
        double difference = firstPower - secondPower;
        String winner = Math.abs(difference) <= DRAW_EPSILON ? null
                : difference > 0.0 ? first.sideId() : second.sideId();
        return new CombatResult(first, second, winner);
    }

    private CombatSideResult withPower(CombatSideResult side, double power) {
        return new CombatSideResult(side.sideId(), side.troopType(), side.initialTroops(),
                side.basePower(), side.counterMultiplier(), power);
    }
}
