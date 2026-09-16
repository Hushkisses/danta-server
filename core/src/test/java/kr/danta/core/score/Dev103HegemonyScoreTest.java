package kr.danta.core.score;

import kr.danta.core.nation.NationState;
import kr.danta.core.nation.NationStatus;
import kr.danta.core.state.GameState;
import kr.danta.core.event.DomainEventBus;
import kr.danta.core.territory.TerritoryService;
import kr.danta.core.territory.PointPosition;
import kr.danta.core.territory.StrategicPoint;
import kr.danta.core.territory.StrategicPointType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class Dev103HegemonyScoreTest {
    @Test
    void scoreIsDerivedFromCurrentOwnershipAndChangesImmediately() {
        GameState state = new GameState();
        state.addNation(new NationState("red", "적국", null, 0L, NationStatus.ACTIVE));
        state.addNation(new NationState("blue", "청국", null, 0L, NationStatus.ACTIVE));
        StrategicPoint capital = point("capital", StrategicPointType.CAPITAL, "red");
        StrategicPoint farm = point("farm", StrategicPointType.FARM, "red");
        state.addStrategicPoint(capital);
        state.addStrategicPoint(farm);

        Map<StrategicPointType, Long> provisional = Map.of(
                StrategicPointType.CAPITAL, 5L,
                StrategicPointType.FARM, 2L);
        HegemonyScoreService scores = new HegemonyScoreService(state, t -> provisional.getOrDefault(t, 0L));

        assertEquals(7L, scores.score("red"));
        assertEquals(0L, scores.score("blue"));

        new TerritoryService(state, new DomainEventBus()).changeOwner("farm", "blue", "DEV-103 test");

        assertEquals(5L, scores.score("red"));
        assertEquals(2L, scores.score("blue"));
    }

    @Test
    void serviceDoesNotInventOrStoreASeparateMutableCurrentScore() {
        GameState state = new GameState();
        state.addNation(new NationState("red", "적국", null, 0L, NationStatus.ACTIVE));
        HegemonyScoreService scores = new HegemonyScoreService(state, ignored -> 0L);

        assertEquals(0L, scores.score("red"));
        assertThrows(IllegalArgumentException.class, () -> scores.score("ghost"));
    }

    private static StrategicPoint point(String id, StrategicPointType type, String owner) {
        return new StrategicPoint(id, id, type, owner,
                new PointPosition("world", 0, 64, 0), 1, Map.of());
    }
}
