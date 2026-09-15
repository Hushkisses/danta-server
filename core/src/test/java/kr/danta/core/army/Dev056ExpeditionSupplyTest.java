package kr.danta.core.army;

import kr.danta.core.economy.StrategicResource;
import kr.danta.core.nation.NationState;
import kr.danta.core.state.GameState;
import kr.danta.core.territory.PointPosition;
import kr.danta.core.territory.StrategicPoint;
import kr.danta.core.territory.StrategicPointType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class Dev056ExpeditionSupplyTest {
    private GameState state() {
        GameState state = new GameState();
        state.addNation(new NationState("red", "Red"));
        state.addStrategicPoint(new StrategicPoint("p1", "P1", StrategicPointType.CAPITAL, "red",
                new PointPosition("world", 0, 64, 0), 1, Map.of()));
        state.addArmy(new ArmyState("a1", "red", "p1", ArmyStatus.STATIONED, 1000));
        return state;
    }

    @Test void lightStandardHeavyLoadDifferentFoodAmounts() {
        assertTrue(ExpeditionSupplyLevel.LIGHT.foodAmount() < ExpeditionSupplyLevel.STANDARD.foodAmount());
        assertTrue(ExpeditionSupplyLevel.STANDARD.foodAmount() < ExpeditionSupplyLevel.HEAVY.foodAmount());
    }

    @Test void loadDeductsNationFoodAndStoresArmySupply() {
        GameState state = state();
        state.getOrCreateStrategicResourceStockpile("red").set(StrategicResource.FOOD, 1000);
        var result = new ExpeditionSupplyService(state).load("a1", ExpeditionSupplyLevel.STANDARD);
        assertEquals(300, result.loadedFood());
        assertEquals(700, result.nationFoodRemaining());
        assertEquals(ExpeditionSupplyLevel.STANDARD, state.army("a1").orElseThrow().expeditionSupplyLevel());
        assertEquals(300, state.army("a1").orElseThrow().carriedFood());
    }

    @Test void insufficientFoodDoesNotPartiallyLoad() {
        GameState state = state();
        state.getOrCreateStrategicResourceStockpile("red").set(StrategicResource.FOOD, 99);
        assertThrows(IllegalStateException.class,
                () -> new ExpeditionSupplyService(state).load("a1", ExpeditionSupplyLevel.LIGHT));
        assertEquals(99, state.getOrCreateStrategicResourceStockpile("red").amount(StrategicResource.FOOD));
        assertEquals(0, state.army("a1").orElseThrow().carriedFood());
    }
}
