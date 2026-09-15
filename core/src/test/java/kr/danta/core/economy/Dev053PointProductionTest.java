package kr.danta.core.economy;

import kr.danta.core.nation.NationState;
import kr.danta.core.state.GameState;
import kr.danta.core.territory.PointPosition;
import kr.danta.core.territory.StrategicPoint;
import kr.danta.core.territory.StrategicPointType;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class Dev053PointProductionTest {
    @Test void thirtyMinuteTickProducesHalfHourlyBaseForOwner() {
        GameState state = new GameState();
        NationState red = new NationState("red", "Red");
        red.setCapitalPointId("farm");
        state.addNation(red);
        state.addStrategicPoint(new StrategicPoint("farm", "Farm", StrategicPointType.FARM, "red",
                new PointPosition("world", 0, 64, 0), 2, Map.of("food", 200L)));
        state.addStrategicPoint(new StrategicPoint("mine", "Mine", StrategicPointType.MINE, "red",
                new PointPosition("world", 1, 64, 0), 2, Map.of("iron", 120L)));
        new StrategicPointProductionService(state).produceOneTick();
        StrategicResourceStockpile stock = state.getOrCreateStrategicResourceStockpile("red");
        assertEquals(100, stock.amount(StrategicResource.FOOD));
        assertEquals(60, stock.amount(StrategicResource.IRON));
    }
    @Test void unownedPointProducesNothing() {
        GameState state = new GameState();
        state.addNation(new NationState("red", "Red"));
        state.addStrategicPoint(new StrategicPoint("forest", "Forest", StrategicPointType.FORESTRY, null,
                new PointPosition("world", 0, 64, 0), 2, Map.of("wood", 160L)));
        new StrategicPointProductionService(state).produceOneTick();
        assertTrue(state.strategicResourceStockpile("red").isEmpty());
    }
    @Test void commercialGoldGoesToTreasury() {
        GameState state = new GameState();
        state.addNation(new NationState("red", "Red"));
        state.addStrategicPoint(new StrategicPoint("market", "Market", StrategicPointType.COMMERCIAL, "red",
                new PointPosition("world", 0, 64, 0), 2, Map.of("gold", 160L)));
        new StrategicPointProductionService(state).produceOneTick();
        assertEquals(80, state.nation("red").orElseThrow().treasury());
    }
}
