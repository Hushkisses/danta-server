package kr.danta.core.army;

import kr.danta.core.nation.NationState;
import kr.danta.core.state.GameState;
import kr.danta.core.territory.PointPosition;
import kr.danta.core.territory.StrategicEdge;
import kr.danta.core.territory.StrategicPoint;
import kr.danta.core.territory.StrategicPointType;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Dev031ArmyOrderTest {
    @Test
    void issuesPendingMoveOrderAcrossOneAdjacentEdge() {
        GameState state = gameStateWithArmyAndGraph();
        ArmyOrderService service = new ArmyOrderService(state, () -> "order-1");

        ArmyOrder order = service.issueMoveOrder("red_first", "red_farm");

        assertEquals("order-1", order.orderId());
        assertEquals("red_first", order.armyId());
        assertEquals(ArmyOrderType.MOVE, order.type());
        assertEquals(ArmyOrderStatus.PENDING, order.status());
        assertEquals(new ArmyRoute("red_capital", "red_farm", "e01"), order.route());
        assertEquals(order, state.armyOrder("red_first").orElseThrow());
        assertEquals(ArmyStatus.STATIONED, state.army("red_first").orElseThrow().status());
    }

    @Test
    void rejectsNonAdjacentSamePointAndUnknownDestinations() {
        GameState state = gameStateWithArmyAndGraph();
        ArmyOrderService service = new ArmyOrderService(state, () -> "order-1");

        assertThrows(IllegalArgumentException.class,
                () -> service.issueMoveOrder("red_first", "red_mine"));
        assertThrows(IllegalArgumentException.class,
                () -> service.issueMoveOrder("red_first", "red_capital"));
        assertThrows(IllegalArgumentException.class,
                () -> service.issueMoveOrder("red_first", "missing"));
    }

    @Test
    void rejectsSecondOrderAndArmiesThatAreNotStationed() {
        GameState state = gameStateWithArmyAndGraph();
        ArmyOrderService service = new ArmyOrderService(state, () -> "order-1");
        service.issueMoveOrder("red_first", "red_farm");

        assertThrows(IllegalArgumentException.class,
                () -> service.issueMoveOrder("red_first", "red_farm"));

        GameState nonStationedState = gameStateWithArmyAndGraph();
        ArmyState army = nonStationedState.army("red_first").orElseThrow();
        army.setStatus(ArmyStatus.IN_BATTLE);
        ArmyOrderService nonStationedService = new ArmyOrderService(nonStationedState, () -> "order-2");
        assertThrows(IllegalStateException.class,
                () -> nonStationedService.issueMoveOrder("red_first", "red_farm"));
    }

    private static GameState gameStateWithArmyAndGraph() {
        GameState state = new GameState();
        state.addNation(new NationState("red", "Red Nation"));
        state.addStrategicPoint(point("red_capital", 0));
        state.addStrategicPoint(point("red_farm", 100));
        state.addStrategicPoint(point("red_mine", -100));
        state.addStrategicEdge(new StrategicEdge(
                "e01", "red_capital", "red_farm", Duration.ofMinutes(2)));
        state.addArmy(new ArmyState(
                "red_first", "red", "red_capital", ArmyStatus.STATIONED, 1_200L));
        return state;
    }

    private static StrategicPoint point(String pointId, int x) {
        return new StrategicPoint(pointId, pointId, StrategicPointType.FARM,
                new PointPosition("world", x, 70, 0), 2);
    }
}
