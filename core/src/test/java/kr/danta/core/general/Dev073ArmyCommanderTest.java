package kr.danta.core.general;

import kr.danta.core.army.ArmyState;
import kr.danta.core.army.ArmyStatus;
import kr.danta.core.nation.NationState;
import kr.danta.core.state.GameState;
import kr.danta.core.territory.StrategicPoint;
import kr.danta.core.territory.StrategicPointType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Dev073ArmyCommanderTest {
    private GameState state() {
        GameState s = new GameState();
        s.addNation(new NationState("red", "Red"));
        s.addNation(new NationState("blue", "Blue"));
        s.addStrategicPoint(new StrategicPoint("p1", "P1", StrategicPointType.CAPITAL, "red"));
        s.addStrategicPoint(new StrategicPoint("p2", "P2", StrategicPointType.NORMAL, "red"));
        s.addGeneral(new GeneralState("g1", "red", GeneralGrade.B, 3));
        s.addGeneral(new GeneralState("g2", "blue", GeneralGrade.C, 2));
        s.addArmy(new ArmyState("a1", "red", "p1", ArmyStatus.STATIONED, 100));
        s.addArmy(new ArmyState("a2", "red", "p1", ArmyStatus.STATIONED, 100));
        s.addArmy(new ArmyState("a3", "red", "p2", ArmyStatus.STATIONED, 100));
        s.addArmy(new ArmyState("b1", "blue", "p1", ArmyStatus.STATIONED, 100));
        return s;
    }

    @Test void assignsOneGeneralToOneFriendlyStationedArmy() {
        GameState s = state();
        new ArmyCommanderService(s).assign("g1", "a1");
        assertEquals("g1", s.army("a1").orElseThrow().commanderGeneralId().orElseThrow());
        assertEquals("a1", s.commandedArmyId("g1").orElseThrow());
    }

    @Test void rejectsEnemyArmyAndDuplicateAssignment() {
        GameState s = state();
        ArmyCommanderService service = new ArmyCommanderService(s);
        assertThrows(IllegalArgumentException.class, () -> service.assign("g1", "b1"));
        service.assign("g1", "a1");
        assertThrows(IllegalStateException.class, () -> service.assign("g1", "a2"));
    }

    @Test void movesCommanderOnlyBetweenCoLocatedStationedArmies() {
        GameState s = state();
        ArmyCommanderService service = new ArmyCommanderService(s);
        service.assign("g1", "a1");
        assertThrows(IllegalStateException.class, () -> service.move("g1", "a3"));
        service.move("g1", "a2");
        assertTrue(s.army("a1").orElseThrow().commanderGeneralId().isEmpty());
        assertEquals("g1", s.army("a2").orElseThrow().commanderGeneralId().orElseThrow());
    }

    @Test void movingArmyCarriesItsCommanderWithoutSeparateTeleportLogic() {
        GameState s = state();
        ArmyCommanderService service = new ArmyCommanderService(s);
        service.assign("g1", "a1");
        s.army("a1").orElseThrow().setStatus(ArmyStatus.MOVING);
        s.army("a1").orElseThrow().setLocationPointId("p2");
        assertEquals("g1", s.army("a1").orElseThrow().commanderGeneralId().orElseThrow());
        assertEquals("a1", s.commandedArmyId("g1").orElseThrow());
    }
}
