package kr.danta.simulator;

import kr.danta.core.DantaCore;
import kr.danta.core.combat.CombatScenario;
import kr.danta.core.combat.CombatSimulationSummary;
import kr.danta.core.combat.DeterministicCombatScenarios;
import kr.danta.core.combat.RepeatedCombatSimulator;

/** DEV-065 repeated scenario runner. CSV export is DEV-066. */
public final class CombatSimulatorApp {
    private static final int DEFAULT_RUNS = 1000;

    private CombatSimulatorApp() {}

    public static void main(String[] args) {
        int runs = parseRuns(args);
        RepeatedCombatSimulator simulator = new RepeatedCombatSimulator();
        System.out.println("Danta Combat Simulator DEV-065 - core " + DantaCore.VERSION + " runs=" + runs);
        for (CombatScenario scenario : DeterministicCombatScenarios.all()) {
            CombatSimulationSummary s = simulator.run(scenario, runs);
            System.out.printf(
                    "%s runs=%d wins=%d/%d draws=%d rates=%.4f/%.4f/%.4f avgLosses=%.2f/%.2f%n",
                    s.scenarioId(), s.runs(), s.firstWins(), s.secondWins(), s.draws(),
                    s.firstWinRate(), s.secondWinRate(), s.drawRate(),
                    s.averageFirstLosses(), s.averageSecondLosses());
        }
    }

    private static int parseRuns(String[] args) {
        if (args == null || args.length == 0) return DEFAULT_RUNS;
        try {
            int runs = Integer.parseInt(args[0]);
            if (runs <= 0) throw new IllegalArgumentException("run count must be positive");
            return runs;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("run count must be an integer", e);
        }
    }
}
