package kr.danta.core.combat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * DEV-060 CombatResolver v1 skeleton.
 *
 * Establishes the design-v0.3 phase pipeline:
 * opening -> frontline -> backline -> mobile -> morale -> retreat/pursuit.
 *
 * DEV-060 deliberately keeps each new phase neutral. Authoritative mechanics are
 * added by DEV-061~064 instead of inventing final balance values here. The
 * existing DEV-041 resolver supplies the current baseline power/counter result,
 * so v1 is regression-compatible while exposing a stable staged extension point.
 */
public final class CombatResolverV1 {
    private final CombatResolver baselineResolver;

    public CombatResolverV1() {
        this(new CombatResolver());
    }

    CombatResolverV1(CombatResolver baselineResolver) {
        this.baselineResolver = Objects.requireNonNull(baselineResolver, "baselineResolver");
    }

    public CombatV1Result resolve(CombatSideInput first, CombatSideInput second) {
        CombatResult baseline = baselineResolver.resolve(first, second);
        double firstPower = baseline.first().effectivePower();
        double secondPower = baseline.second().effectivePower();

        List<CombatPhaseResult> phases = new ArrayList<>(CombatPhase.values().length);
        for (CombatPhase phase : CombatPhase.values()) {
            CombatPhaseResult result = resolveNeutralPhase(phase, firstPower, secondPower);
            phases.add(result);
            firstPower = result.firstPower();
            secondPower = result.secondPower();
        }

        CombatResult finalResult = withFinalPower(baseline, firstPower, secondPower);
        return new CombatV1Result(finalResult, phases);
    }

    private CombatPhaseResult resolveNeutralPhase(CombatPhase phase, double firstPower, double secondPower) {
        return CombatPhaseResult.neutral(phase, firstPower, secondPower);
    }

    private CombatResult withFinalPower(CombatResult baseline, double firstPower, double secondPower) {
        CombatSideResult first = withPower(baseline.first(), firstPower);
        CombatSideResult second = withPower(baseline.second(), secondPower);
        String winner = Double.compare(firstPower, secondPower) == 0 ? null
                : firstPower > secondPower ? first.sideId() : second.sideId();
        return new CombatResult(first, second, winner);
    }

    private CombatSideResult withPower(CombatSideResult side, double power) {
        return new CombatSideResult(side.sideId(), side.troopType(), side.initialTroops(),
                side.basePower(), side.counterMultiplier(), power);
    }
}
