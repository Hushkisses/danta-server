package kr.danta.simulator;

import kr.danta.core.DantaCore;
import kr.danta.core.combat.CombatResolver;
import kr.danta.core.combat.CombatResult;
import kr.danta.core.combat.CombatScenario;
import kr.danta.core.combat.CombatLossPolicy;
import kr.danta.core.combat.CombatResolution;
import kr.danta.core.combat.DeterministicCombatScenarios;

/** DEV-042 deterministic scenario runner. */
public final class CombatSimulatorApp {
    private CombatSimulatorApp() {}

    public static void main(String[] args) {
        CombatResolver resolver = new CombatResolver();
        CombatLossPolicy lossPolicy = new CombatLossPolicy();
        System.out.println("Danta Combat Simulator DEV - core " + DantaCore.VERSION);
        for (CombatScenario scenario : DeterministicCombatScenarios.all()) {
            CombatResult result = resolver.resolve(scenario.first(), scenario.second());
            CombatResolution resolution = lossPolicy.apply(result);
            System.out.printf("%s red=%.2f blue=%.2f winner=%s losses=%d/%d remaining=%d/%d retreat=%s/%s%n",
                    scenario.id(), result.first().effectivePower(), result.second().effectivePower(),
                    result.winner().orElse("DRAW"),
                    resolution.first().losses(), resolution.second().losses(),
                    resolution.first().remainingTroops(), resolution.second().remainingTroops(),
                    resolution.first().retreatRequired(), resolution.second().retreatRequired());
        }
    }
}
