package kr.danta.core.army;

import kr.danta.core.nation.NationState;
import kr.danta.core.state.GameState;
import kr.danta.core.territory.BattlefieldTag;
import kr.danta.core.territory.PointPosition;
import kr.danta.core.territory.StrategicEdge;
import kr.danta.core.territory.StrategicPoint;
import kr.danta.core.territory.StrategicPointType;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Dev032ArmyMovementTimeTest {
    @Test
    void usesBaseTimeWhenRouteHasNoMovementModifier() {
        GameState state = stateWithEdge(Set.of(), 120_000L);
        ArmyMovementTime time = issueAndCalculate(state);
        assertEquals(Duration.ofSeconds(120), time.baseDuration());
        assertEquals(Duration.ofSeconds(120), time.effectiveDuration());
        assertEquals(1.0D, time.multiplier(), 0.000001D);
    }

    @Test
    void roadReducesTravelTime() {
        GameState state = stateWithEdge(Set.of(BattlefieldTag.ROAD, BattlefieldTag.PLAIN), 120_000L);
        ArmyMovementTime time = issueAndCalculate(state);
        assertEquals(Duration.ofSeconds(108), time.effectiveDuration());
        assertEquals(0.90D, time.multiplier(), 0.000001D);
    }

    @Test
    void mountainPassIncreasesTravelTime() {
        GameState state = stateWithEdge(Set.of(BattlefieldTag.MOUNTAIN_PASS), 150_000L);
        ArmyMovementTime time = issueAndCalculate(state);
        assertEquals(Duration.ofSeconds(180), time.effectiveDuration());
        assertEquals(1.20D, time.multiplier(), 0.000001D);
    }

    @Test
    void combinesIndependentRouteModifiers() {
        GameState state = stateWithEdge(Set.of(BattlefieldTag.ROAD, BattlefieldTag.CANYON), 200_000L);
        ArmyMovementTime time = issueAndCalculate(state);
        assertEquals(Duration.ofMillis(207_000L), time.effectiveDuration());
        assertEquals(1.035D, time.multiplier(), 0.000001D);
    }

    @Test
    void rejectsStaleOrderOrigin() {
        GameState state = stateWithEdge(Set.of(BattlefieldTag.ROAD), 120_000L);
        new ArmyOrderService(state, () -> "order-1").issueMoveOrder("red_first", "red_farm");
        state.army("red_first").orElseThrow().setLocationPointId("red_mine");
        assertThrows(IllegalStateException.class,
                () -> new ArmyMovementTimeService(state).calculateForArmy("red_first"));
    }

    private static ArmyMovementTime issueAndCalculate(GameState state) {
        new ArmyOrderService(state, () -> "order-1").issueMoveOrder("red_first", "red_farm");
        return new ArmyMovementTimeService(state).calculateForArmy("red_first");
    }

    private static GameState stateWithEdge(Set<BattlefieldTag> tags, long baseTravelMillis) {
        GameState state = new GameState();
        state.addNation(new NationState("red", "Red Nation"));
        state.addStrategicPoint(point("red_capital", 0));
        state.addStrategicPoint(point("red_farm", 100));
        state.addStrategicPoint(point("red_mine", -100));
        state.addStrategicEdge(new StrategicEdge(
                "e01", "red_capital", "red_farm", baseTravelMillis, tags));
        state.addArmy(new ArmyState(
                "red_first", "red", "red_capital", ArmyStatus.STATIONED, 1_200L));
        return state;
    }

    private static StrategicPoint point(String pointId, int x) {
        return new StrategicPoint(pointId, pointId, StrategicPointType.FARM,
                new PointPosition("world", x, 70, 0), 2);
    }
}
