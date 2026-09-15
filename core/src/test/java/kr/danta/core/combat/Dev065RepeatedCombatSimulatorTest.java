package kr.danta.core.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Dev065RepeatedCombatSimulatorTest {
    private final RepeatedCombatSimulator simulator = new RepeatedCombatSimulator();

    @Test void repeatsWinningScenarioAndAggregatesRatesAndLosses() {
        CombatScenario scenario = DeterministicCombatScenarios.all().stream()
                .filter(s -> s.id().equals("infantry-beats-spearmen")).findFirst().orElseThrow();
        CombatSimulationSummary s = simulator.run(scenario, 1000);

        assertEquals(1000, s.runs());
        assertEquals(1000, s.firstWins());
        assertEquals(0, s.secondWins());
        assertEquals(0, s.draws());
        assertEquals(1.0, s.firstWinRate(), 0.000001);
        assertEquals(0.0, s.secondWinRate(), 0.000001);
        assertEquals(100.0, s.averageFirstLosses(), 0.000001);
        assertEquals(250.0, s.averageSecondLosses(), 0.000001);
    }

    @Test void repeatsDrawScenarioWithoutInventingRandomness() {
        CombatScenario scenario = DeterministicCombatScenarios.all().stream()
                .filter(s -> s.id().equals("equal-infantry-draw")).findFirst().orElseThrow();
        CombatSimulationSummary s = simulator.run(scenario, 500);

        assertEquals(500, s.draws());
        assertEquals(1.0, s.drawRate(), 0.000001);
        assertEquals(100.0, s.averageFirstLosses(), 0.000001);
        assertEquals(100.0, s.averageSecondLosses(), 0.000001);
    }

    @Test void rejectsNonPositiveRunCount() {
        CombatScenario scenario = DeterministicCombatScenarios.all().getFirst();
        assertThrows(IllegalArgumentException.class, () -> simulator.run(scenario, 0));
        assertThrows(IllegalArgumentException.class, () -> simulator.run(scenario, -1));
    }

    @Test void v1RepeatedRunnerPreservesAllExistingDeterministicScenarios() {
        for (CombatScenario scenario : DeterministicCombatScenarios.all()) {
            CombatSimulationSummary s = simulator.run(scenario, 10);
            if (scenario.expectsDraw()) {
                assertEquals(10, s.draws(), scenario.id());
            } else if ("red".equals(scenario.expectedWinnerSideId())) {
                assertEquals(10, s.firstWins(), scenario.id());
            } else {
                assertEquals(10, s.secondWins(), scenario.id());
            }
        }
    }
}
