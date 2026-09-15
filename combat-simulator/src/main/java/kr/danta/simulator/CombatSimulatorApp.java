package kr.danta.simulator;

import kr.danta.core.DantaCore;
import kr.danta.core.combat.CombatResolver;
import kr.danta.core.combat.CombatResult;
import kr.danta.core.combat.CombatScenario;
import kr.danta.core.combat.DeterministicCombatScenarios;

/** DEV-042 deterministic scenario runner. */
public final class CombatSimulatorApp {
    private CombatSimulatorApp() {}

    public static void main(String[] args) {
        CombatResolver resolver = new CombatResolver();
        System.out.println("Danta Combat Simulator DEV - core " + DantaCore.VERSION);
        for (CombatScenario scenario : DeterministicCombatScenarios.all()) {
            CombatResult result = resolver.resolve(scenario.first(), scenario.second());
            System.out.printf("%s red=%.2f blue=%.2f winner=%s%n",
                    scenario.id(), result.first().effectivePower(), result.second().effectivePower(),
                    result.winner().orElse("DRAW"));
        }
    }
}
