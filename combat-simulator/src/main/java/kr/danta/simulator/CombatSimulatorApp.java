package kr.danta.simulator;

import kr.danta.core.DantaCore;
import kr.danta.core.combat.CombatScenario;
import kr.danta.core.combat.CombatSimulationCsv;
import kr.danta.core.combat.CombatSimulationSummary;
import kr.danta.core.combat.DeterministicCombatScenarios;
import kr.danta.core.combat.RepeatedCombatSimulator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** DEV-066 repeated scenario runner with optional CSV balance report. */
public final class CombatSimulatorApp {
    private static final int DEFAULT_RUNS = 1000;

    private CombatSimulatorApp() {}

    public static void main(String[] args) throws IOException {
        int runs = parseRuns(args);
        Path csvPath = parseCsvPath(args);
        RepeatedCombatSimulator simulator = new RepeatedCombatSimulator();
        List<CombatSimulationSummary> summaries = new ArrayList<>();

        System.out.println("Danta Combat Simulator DEV-066 - core " + DantaCore.VERSION + " runs=" + runs);
        for (CombatScenario scenario : DeterministicCombatScenarios.all()) {
            CombatSimulationSummary s = simulator.run(scenario, runs);
            summaries.add(s);
            System.out.printf(
                    "%s runs=%d wins=%d/%d draws=%d rates=%.4f/%.4f/%.4f avgLosses=%.2f/%.2f%n",
                    s.scenarioId(), s.runs(), s.firstWins(), s.secondWins(), s.draws(),
                    s.firstWinRate(), s.secondWinRate(), s.drawRate(),
                    s.averageFirstLosses(), s.averageSecondLosses());
        }

        if (csvPath != null) {
            Path parent = csvPath.toAbsolutePath().getParent();
            if (parent != null) Files.createDirectories(parent);
            Files.writeString(csvPath, CombatSimulationCsv.format(summaries), StandardCharsets.UTF_8);
            System.out.println("CSV report: " + csvPath.toAbsolutePath());
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

    private static Path parseCsvPath(String[] args) {
        if (args == null || args.length < 2 || args[1].isBlank()) return null;
        return Path.of(args[1]);
    }
}
