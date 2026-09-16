package kr.danta.core.diplomacy;
import kr.danta.core.nation.NationState;import kr.danta.core.state.GameState;import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class Dev090DiplomacyServiceTest {
 private DiplomacyService service(){GameState g=new GameState();g.addNation(new NationState("red","적국"));g.addNation(new NationState("blue","청국"));return new DiplomacyService(g);}
 @Test void relationIsSymmetric(){var s=service();s.setStatus("red","blue",DiplomaticStatus.FRIENDLY);assertEquals(DiplomaticStatus.FRIENDLY,s.status("blue","red"));assertEquals(1,s.relations().size());}
 @Test void supportsAllianceAndWar(){var s=service();s.setStatus("red","blue",DiplomaticStatus.ALLIANCE);assertEquals(DiplomaticStatus.ALLIANCE,s.status("red","blue"));s.setStatus("red","blue",DiplomaticStatus.WAR);assertEquals(DiplomaticStatus.WAR,s.status("blue","red"));}
 @Test void neutralRemovesExplicitRelation(){var s=service();s.setStatus("red","blue",DiplomaticStatus.WAR);s.setStatus("red","blue",DiplomaticStatus.NEUTRAL);assertTrue(s.relations().isEmpty());assertEquals(DiplomaticStatus.NEUTRAL,s.status("red","blue"));}
 @Test void rejectsSelfOrUnknownNation(){var s=service();assertThrows(IllegalArgumentException.class,()->s.setStatus("red","red",DiplomaticStatus.WAR));assertThrows(IllegalArgumentException.class,()->s.status("red","ghost"));}
}
