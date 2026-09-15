package kr.danta.core.combat;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Dev066CombatSimulationCsvTest {
    @Test void writesStableHeaderAndSummaryRow() {
        CombatSimulationSummary summary = new CombatSimulationSummary(
                "infantry-beats-spearmen", 1000, 1000, 0, 0,
                1.0, 0.0, 0.0, 100.0, 250.0);

        String csv = CombatSimulationCsv.format(List.of(summary));
        String[] lines = csv.split("\\R");

        assertEquals(CombatSimulationCsv.HEADER, lines[0]);
        assertEquals("infantry-beats-spearmen,1000,1000,0,0,1.000000,0.000000,0.000000,100.000000,250.000000", lines[1]);
    }

    @Test void usesLocaleIndependentDecimalPoint() {
        CombatSimulationSummary summary = new CombatSimulationSummary(
                "test", 3, 1, 1, 1, 1.0 / 3.0, 1.0 / 3.0, 1.0 / 3.0, 12.5, 20.25);
        String csv = CombatSimulationCsv.format(List.of(summary));
        assertTrue(csv.contains("0.333333"));
        assertTrue(csv.contains("12.500000,20.250000"));
    }

    @Test void escapesScenarioIdWhenRequiredByCsv() {
        CombatSimulationSummary summary = new CombatSimulationSummary(
                "a,b\"c", 1, 1, 0, 0, 1.0, 0.0, 0.0, 1.0, 2.0);
        String csv = CombatSimulationCsv.format(List.of(summary));
        assertTrue(csv.contains("\"a,b\"\"c\""));
    }
}
