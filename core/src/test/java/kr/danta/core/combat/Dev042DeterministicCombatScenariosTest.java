package kr.danta.core.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Dev042DeterministicCombatScenariosTest {
    private static final double EPSILON = 0.000001;

    @Test void everyFixedScenarioMatchesExpectedResult() {
        CombatResolver resolver = new CombatResolver();
        for (CombatScenario scenario : DeterministicCombatScenarios.all()) {
            CombatResult result = resolver.resolve(scenario.first(), scenario.second());
            assertEquals(scenario.expectedFirstPower(), result.first().effectivePower(), EPSILON, scenario.id());
            assertEquals(scenario.expectedSecondPower(), result.second().effectivePower(), EPSILON, scenario.id());
            if (scenario.expectsDraw()) {
                assertTrue(result.draw(), scenario.id());
            } else {
                assertEquals(scenario.expectedWinnerSideId(), result.winner().orElseThrow(), scenario.id());
            }
        }
    }

    @Test void scenarioSetCoversAllFourSoftCountersAndNonHardCounterOutcome() {
        assertEquals(6, DeterministicCombatScenarios.all().size());
        assertTrue(DeterministicCombatScenarios.all().stream().anyMatch(s -> s.id().equals("infantry-beats-spearmen")));
        assertTrue(DeterministicCombatScenarios.all().stream().anyMatch(s -> s.id().equals("spearmen-beats-cavalry")));
        assertTrue(DeterministicCombatScenarios.all().stream().anyMatch(s -> s.id().equals("cavalry-beats-archers")));
        assertTrue(DeterministicCombatScenarios.all().stream().anyMatch(s -> s.id().equals("archers-beat-infantry")));
        assertTrue(DeterministicCombatScenarios.all().stream().anyMatch(s -> s.id().equals("numbers-overcome-soft-counter")));
    }
}
