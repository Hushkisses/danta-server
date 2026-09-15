package kr.danta.core.economy;

import kr.danta.core.army.ArmyState;
import kr.danta.core.army.ArmyStatus;
import kr.danta.core.nation.NationState;
import kr.danta.core.state.GameState;
import kr.danta.core.territory.PointPosition;
import kr.danta.core.territory.StrategicPoint;
import kr.danta.core.territory.StrategicPointType;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Dev055ArmySupplyTest {
    @Test void actionConsumesMuchMoreFoodThanNormalStationing() {
        GameState state = new GameState();
        state.addNation(new NationState("red", "Red"));
        state.addStrategicPoint(new StrategicPoint("p1", "P1", StrategicPointType.CAPITAL, "red", new PointPosition("world", 0, 64, 0), 1, Map.of()));
        ArmyState army = new ArmyState("a1", "red", "p1", ArmyStatus.STATIONED, 1000);
        state.addArmy(army);
        ArmySupplyService service = new ArmySupplyService(state);
        assertEquals(1, service.foodNeed(army));
        army.setStatus(ArmyStatus.MOVING);
        assertEquals(20, service.foodNeed(army));
        army.setStatus(ArmyStatus.IN_BATTLE);
        assertEquals(40, service.foodNeed(army));
    }

    @Test void tickNeverMakesFoodNegativeAndReportsShortfall() {
        GameState state = new GameState();
        state.addNation(new NationState("red", "Red"));
        state.addStrategicPoint(new StrategicPoint("p1", "P1", StrategicPointType.CAPITAL, "red", new PointPosition("world", 0, 64, 0), 1, Map.of()));
        state.addArmy(new ArmyState("a1", "red", "p1", ArmyStatus.MOVING, 1000));
        state.getOrCreateStrategicResourceStockpile("red").set(StrategicResource.FOOD, 7);
        ArmySupplyService.TickResult result = new ArmySupplyService(state).consumeOneTick();
        assertEquals(20, result.requestedFood());
        assertEquals(7, result.consumedFood());
        assertEquals(0, state.getOrCreateStrategicResourceStockpile("red").amount(StrategicResource.FOOD));
        assertEquals(1, result.shortfallArmies());
    }
}
