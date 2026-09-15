package kr.danta.core.army;

import kr.danta.core.nation.NationState;
import kr.danta.core.state.GameState;
import kr.danta.core.territory.PointPosition;
import kr.danta.core.territory.StrategicPoint;
import kr.danta.core.territory.StrategicPointType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Dev035ArmyAdvanceStopPolicyTest {
    @Test void stopsAtMajorPoint() {
        GameState state = state(StrategicPointType.MAJOR);
        ArmyAdvanceDecision decision = new ArmyAdvanceStopPolicy(state).evaluateAfterArrival("army");
        assertTrue(decision.shouldStop());
        assertEquals(ArmyAdvanceStopReason.MAJOR_POINT, decision.reason());
    }

    @Test void continuesAtNormalPoint() {
        GameState state = state(StrategicPointType.FARM);
        assertFalse(new ArmyAdvanceStopPolicy(state).evaluateAfterArrival("army").shouldStop());
    }

    @Test void exposesFutureCombatAndSupplyHooks() {
        ArmyAdvanceStopPolicy policy = new ArmyAdvanceStopPolicy(state(StrategicPointType.FARM));
        assertEquals(ArmyAdvanceStopReason.COMBAT, policy.combatStop().reason());
        assertEquals(ArmyAdvanceStopReason.SUPPLY, policy.supplyStop().reason());
    }

    private static GameState state(StrategicPointType type) {
        GameState state = new GameState();
        state.addNation(new NationState("red", "Red"));
        state.addStrategicPoint(new StrategicPoint("p", "Point", type,
                new PointPosition("world", 0, 70, 0), 1));
        state.addArmy(new ArmyState("army", "red", "p", ArmyStatus.STATIONED, 100));
        return state;
    }
}
