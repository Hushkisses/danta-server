package kr.danta.paper.combat.ai;

import kr.danta.paper.combat.live.CombatSide;
import kr.danta.paper.combat.live.LiveCombatDemoFormation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Dev116CombatAiBenchmarkTest {

    @Test
    void parsesSupportedBenchmarkCommandsAndCounts() {
        DantaCombatAiCommandBridge bridge = new DantaCombatAiCommandBridge();

        assertEquals(new DantaCombatAiCommandBridge.BenchmarkAction.Start(40),
                bridge.parseBenchmarkAction(new String[]{"combat-ai", "benchmark", "start", "40"}).orElseThrow());
        assertEquals(new DantaCombatAiCommandBridge.BenchmarkAction.Status(),
                bridge.parseBenchmarkAction(new String[]{"combat-ai", "benchmark", "status"}).orElseThrow());
        assertEquals(new DantaCombatAiCommandBridge.BenchmarkAction.Stop(),
                bridge.parseBenchmarkAction(new String[]{"combat-ai", "benchmark", "stop"}).orElseThrow());
        assertTrue(bridge.parseBenchmarkAction(
                new String[]{"combat-ai", "benchmark", "start", "20"}).isEmpty());
    }

    @Test
    void benchmarkFormationCreatesExactBalancedUnitCount() {
        LiveCombatDemoFormation formation = LiveCombatDemoFormation.benchmark(40);

        assertEquals(40, formation.slots().size());
        assertEquals(20, formation.slots().stream().filter(slot -> slot.side() == CombatSide.RED).count());
        assertEquals(20, formation.slots().stream().filter(slot -> slot.side() == CombatSide.BLUE).count());
    }

    @Test
    void benchmarkSessionTracksWorstAndAverageMetrics() {
        CombatAiBenchmarkSession session = new CombatAiBenchmarkSession();
        assertTrue(session.start(40, 1_000L));

        session.sample(38, 2_000L, 20.0, 35.0);
        session.sample(30, 3_000L, 19.5, 45.0);

        CombatAiBenchmarkSession.Snapshot snapshot = session.snapshot(3_000L);
        assertTrue(snapshot.active());
        assertEquals(40, snapshot.targetUnits());
        assertEquals(30, snapshot.aliveUnits());
        assertEquals(2_000L, snapshot.elapsedMillis());
        assertEquals(19.5, snapshot.minimumTps(), 0.0001);
        assertEquals(40.0, snapshot.averageMspt(), 0.0001);
        assertEquals(45.0, snapshot.maximumMspt(), 0.0001);
    }
}
