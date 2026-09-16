package kr.danta.paper;

import kr.danta.paper.combat.ai.DantaCombatAiCommandBridge;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Dev114DantaPluginCombatAiRoutingTest {

    @Test
    void routesCombatAiThroughDantaPluginBoundary() {
        DantaCombatAiCommandBridge.Result result = DantaPlugin.routeCombatAiCommand(
                new String[]{"combat-ai", "profiles"});

        assertTrue(result.handled());
        assertTrue(result.message().contains("보병"));
        assertTrue(result.message().contains("마법병"));
    }

    @Test
    void leavesOtherDantaCommandsUntouched() {
        DantaCombatAiCommandBridge.Result result = DantaPlugin.routeCombatAiCommand(
                new String[]{"runtime", "status"});

        assertFalse(result.handled());
        assertEquals("", result.message());
    }
}
