package kr.danta.paper.combat.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DantaCombatAiRuntimeCommandTest {

    @Test
    void claimsOnlyCombatAiDemoNamespace() {
        assertTrue(DantaCombatAiRuntime.isDemoCommand("/danta combat-ai demo"));
        assertTrue(DantaCombatAiRuntime.isDemoCommand("/danta combat-ai demo start"));
        assertTrue(DantaCombatAiRuntime.isDemoCommand("danta combat-ai demo status"));

        assertFalse(DantaCombatAiRuntime.isDemoCommand("/danta combat-ai profiles"));
        assertFalse(DantaCombatAiRuntime.isDemoCommand("/danta combat-ai decide cavalry exposed-backline"));
        assertFalse(DantaCombatAiRuntime.isDemoCommand("/danta siege status red_capital"));
        assertFalse(DantaCombatAiRuntime.isDemoCommand(null));
    }
}
