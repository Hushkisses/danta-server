package kr.danta.core.combat;

import java.util.Objects;

/**
 * DEV-065 repeated-combat simulation service.
 *
 * Current CombatResolver v1 is deterministic, so repeated identical inputs must
 * produce identical outcomes. This runner intentionally does not inject random
 * noise merely to manufacture a distribution; it establishes the aggregation
 * harness that later stochastic/balance rules can use.
 */
public final class RepeatedCombatSimulator {
    private final CombatResolverV1 resolver;
    private final CombatLossPolicy lossPolicy;

    public RepeatedCombatSimulator() {
        this(new CombatResolverV1(), new CombatLossPolicy());
    }

    RepeatedCombatSimulator(CombatResolverV1 resolver, CombatLossPolicy lossPolicy) {
        this.resolver = Objects.requireNonNull(resolver, "resolver");
        this.lossPolicy = Objects.requireNonNull(lossPolicy, "lossPolicy");
    }

    public CombatSimulationSummary run(CombatScenario scenario, int runs) {
        Objects.requireNonNull(scenario, "scenario");
        if (runs <= 0) throw new IllegalArgumentException("runs must be positive");

        int firstWins = 0, secondWins = 0, draws = 0;
        long firstLosses = 0L, secondLosses = 0L;

        for (int i = 0; i < runs; i++) {
            CombatV1Result combat = resolver.resolve(scenario.first(), scenario.second());
            CombatResult result = combat.finalResult();
            if (result.draw()) draws++;
            else if (result.first().sideId().equals(result.winner().orElseThrow())) firstWins++;
            else secondWins++;

            CombatResolution resolution = lossPolicy.apply(result);
            firstLosses = Math.addExact(firstLosses, resolution.first().losses());
            secondLosses = Math.addExact(secondLosses, resolution.second().losses());
        }

        return new CombatSimulationSummary(
                scenario.id(), runs, firstWins, secondWins, draws,
                firstWins / (double) runs, secondWins / (double) runs, draws / (double) runs,
                firstLosses / (double) runs, secondLosses / (double) runs
        );
    }
}
