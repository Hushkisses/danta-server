package kr.danta.paper.combat.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Dev115CombatAiDemoCommandTest {

    @Test
    void parsesDemoCommandsWithoutBreakingExistingValidationCommands() {
        DantaCombatAiCommandBridge bridge = new DantaCombatAiCommandBridge();

        assertTrue(bridge.execute(new String[]{"combat-ai", "profiles"}).handled());
        assertEquals(DantaCombatAiCommandBridge.DemoAction.START,
                bridge.parseDemoAction(new String[]{"combat-ai", "demo", "start"}).orElseThrow());
        assertEquals(DantaCombatAiCommandBridge.DemoAction.STOP,
                bridge.parseDemoAction(new String[]{"combat-ai", "demo", "stop"}).orElseThrow());
        assertEquals(DantaCombatAiCommandBridge.DemoAction.STATUS,
                bridge.parseDemoAction(new String[]{"combat-ai", "demo", "status"}).orElseThrow());
    }

    @Test
    void demoCommandIsRecognizedButLeftForPaperBoundaryExecution() {
        DantaCombatAiCommandBridge bridge = new DantaCombatAiCommandBridge();

        DantaCombatAiCommandBridge.Result result = bridge.execute(
                new String[]{"combat-ai", "demo", "start"});

        assertTrue(result.handled());
        assertTrue(result.message().contains("전투 시연"));
    }
}
