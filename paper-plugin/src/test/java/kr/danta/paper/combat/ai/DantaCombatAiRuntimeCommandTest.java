package kr.danta.paper.combat.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DantaCombatAiRuntimeCommandTest {

    @Test
    void claimsOnlyCombatAiLiveRuntimeNamespaces() {
        assertTrue(DantaCombatAiRuntime.isRuntimeCommand("/danta combat-ai demo"));
        assertTrue(DantaCombatAiRuntime.isRuntimeCommand("/danta combat-ai demo start"));
        assertTrue(DantaCombatAiRuntime.isRuntimeCommand("danta combat-ai demo status"));
        assertTrue(DantaCombatAiRuntime.isRuntimeCommand("/danta combat-ai benchmark start 40"));
        assertTrue(DantaCombatAiRuntime.isRuntimeCommand("/danta combat-ai benchmark status"));

        assertFalse(DantaCombatAiRuntime.isRuntimeCommand("/danta combat-ai profiles"));
        assertFalse(DantaCombatAiRuntime.isRuntimeCommand("/danta combat-ai decide cavalry exposed-backline"));
        assertFalse(DantaCombatAiRuntime.isRuntimeCommand("/danta siege status red_capital"));
        assertFalse(DantaCombatAiRuntime.isRuntimeCommand(null));
    }
}
