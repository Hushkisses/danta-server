package kr.danta.core.army;

import kr.danta.core.nation.NationState;
import kr.danta.core.state.GameState;
import kr.danta.core.territory.PointPosition;
import kr.danta.core.territory.StrategicEdge;
import kr.danta.core.territory.StrategicPoint;
import kr.danta.core.territory.StrategicPointType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class Dev034ArmyOperationQueueTest {
    @Test void acceptsAdjacentSequentialRoute() {
        GameState state = state();
        ArmyOperationQueue queue = new ArmyOperationQueueService(state)
                .create("army", List.of("b", "c"));
        assertEquals(List.of("b", "c"), queue.destinations());
        assertEquals("b", queue.nextDestination());
        assertEquals(List.of("c"), queue.afterArrival().destinations());
    }

    @Test void rejectsNonAdjacentLeg() {
        GameState state = state();
        assertThrows(IllegalArgumentException.class,
                () -> new ArmyOperationQueueService(state).create("army", List.of("c")));
    }

    private static GameState state() {
        GameState state = new GameState();
        state.addNation(new NationState("red", "Red"));
        state.addStrategicPoint(point("a", 0));
        state.addStrategicPoint(point("b", 1));
        state.addStrategicPoint(point("c", 2));
        state.addStrategicEdge(new StrategicEdge("ab", "a", "b", 1000, Set.of()));
        state.addStrategicEdge(new StrategicEdge("bc", "b", "c", 1000, Set.of()));
        state.addArmy(new ArmyState("army", "red", "a", ArmyStatus.STATIONED, 100));
        return state;
    }
    private static StrategicPoint point(String id, int x) {
        return new StrategicPoint(id, id, StrategicPointType.FARM, new PointPosition("world", x, 70, 0), 1);
    }
}
