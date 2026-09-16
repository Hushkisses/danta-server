package kr.danta.paper.combat.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Dev114CombatAiCommandBridgeTest {

    private final DantaCombatAiCommandBridge bridge = new DantaCombatAiCommandBridge();

    @Test
    void recognizesCombatAiSubcommandAndStripsDantaPrefix() {
        DantaCombatAiCommandBridge.Result result = bridge.execute(
                new String[]{"combat-ai", "profiles"});

        assertTrue(result.handled());
        assertTrue(result.message().contains("보병"));
        assertTrue(result.message().contains("마법병"));
    }

    @Test
    void routesDecisionArgumentsToValidationRuntime() {
        DantaCombatAiCommandBridge.Result result = bridge.execute(
                new String[]{"combat-ai", "decide", "cavalry", "exposed-backline"});

        assertTrue(result.handled());
        assertTrue(result.message().contains("기병"));
        assertTrue(result.message().contains("우회"));
    }

    @Test
    void ignoresUnrelatedDantaSubcommands() {
        DantaCombatAiCommandBridge.Result result = bridge.execute(
                new String[]{"runtime", "status"});

        assertFalse(result.handled());
        assertEquals("", result.message());
    }
}
