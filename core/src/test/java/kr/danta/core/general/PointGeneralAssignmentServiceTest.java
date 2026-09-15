package kr.danta.core.general;

import kr.danta.core.army.ArmyState;
import kr.danta.core.army.ArmyStatus;
import kr.danta.core.nation.NationState;
import kr.danta.core.state.GameState;
import kr.danta.core.territory.PointPosition;
import kr.danta.core.territory.StrategicPoint;
import kr.danta.core.territory.StrategicPointType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PointGeneralAssignmentServiceTest {
    private GameState state() {
        GameState s = new GameState();
        s.addNation(new NationState("red", "Red"));
        s.addNation(new NationState("blue", "Blue"));
        s.addStrategicPoint(new StrategicPoint("p1","P1", StrategicPointType.CAPITAL,"red",new PointPosition("world",0,64,0),4, Map.of()));
        s.addStrategicPoint(new StrategicPoint("p2","P2", StrategicPointType.FARM,"red",new PointPosition("world",10,64,0),2, Map.of()));
        s.addStrategicPoint(new StrategicPoint("b1","B1", StrategicPointType.FARM,"blue",new PointPosition("world",20,64,0),2, Map.of()));
        s.addGeneral(new GeneralState("g1","red",GeneralGrade.A,1));
        s.addGeneral(new GeneralState("g2","red",GeneralGrade.A,1));
        s.addArmy(new ArmyState("a1","red","p1", ArmyStatus.STATIONED,100));
        return s;
    }

    @Test void onlyOneGeneralMayOccupyPoint() {
        GameState s=state(); PointGeneralAssignmentService service=new PointGeneralAssignmentService(s);
        service.assign("g1","p1");
        assertEquals("g1",s.strategicPoint("p1").orElseThrow().assignedGeneralId().orElseThrow());
        assertThrows(IllegalStateException.class,()->service.assign("g2","p1"));
    }

    @Test void pointAssignmentRequiresFriendlyOwnedPoint() {
        GameState s=state(); PointGeneralAssignmentService service=new PointGeneralAssignmentService(s);
        assertThrows(IllegalArgumentException.class,()->service.assign("g1","b1"));
    }

    @Test void armyCommandAndPointAssignmentAreExclusive() {
        GameState s=state();
        new ArmyCommanderService(s).assign("g1","a1");
        assertThrows(IllegalStateException.class,()->new PointGeneralAssignmentService(s).assign("g1","p1"));
    }

    @Test void assignedGeneralCanMoveToEmptyFriendlyPoint() {
        GameState s=state(); PointGeneralAssignmentService service=new PointGeneralAssignmentService(s);
        service.assign("g1","p1"); service.move("g1","p2");
        assertTrue(s.strategicPoint("p1").orElseThrow().assignedGeneralId().isEmpty());
        assertEquals("g1",s.strategicPoint("p2").orElseThrow().assignedGeneralId().orElseThrow());
    }
}
