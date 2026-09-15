package kr.danta.core.economy;

import kr.danta.core.nation.NationState;
import kr.danta.core.state.GameState;
import kr.danta.core.territory.PointPosition;
import kr.danta.core.territory.StrategicPoint;
import kr.danta.core.territory.StrategicPointType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class Dev057AdministrativeCapacityTest {
    @Test void v03DemandWeightsAndBaseCapacityAreApplied() {
        GameState state = new GameState();
        state.addNation(new NationState("red", "Red"));
        add(state, "cap", StrategicPointType.CAPITAL);
        add(state, "normal", StrategicPointType.FARM);
        add(state, "port", StrategicPointType.PORT);
        add(state, "major", StrategicPointType.MAJOR);
        var status = new AdministrativeCapacityService(state).status("red");
        assertEquals(4, status.ownedPoints());
        assertEquals(4.25, status.demand(), 0.001);
        assertEquals(6.0, status.capacity(), 0.001);
        assertFalse(status.overextended());
        assertEquals(1.0, status.revenueMultiplier(), 0.001);
    }

    @Test void exceedingCapacityAppliesProgressiveRevenuePenaltyWithoutCombatPenalty() {
        GameState state = new GameState();
        state.addNation(new NationState("red", "Red"));
        for (int i = 0; i < 8; i++) add(state, "p" + i, StrategicPointType.FARM);
        var status = new AdministrativeCapacityService(state).status("red");
        assertEquals(8.0, status.demand(), 0.001);
        assertEquals(2.0, status.excess(), 0.001);
        assertEquals(0.80, status.revenueMultiplier(), 0.001);
    }

    private static void add(GameState state, String id, StrategicPointType type) {
        state.addStrategicPoint(new StrategicPoint(id, id, type, "red",
                new PointPosition("world", 0, 64, 0), 1, Map.of()));
    }
}
