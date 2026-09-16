package kr.danta.core.diplomacy;
import kr.danta.core.nation.NationState;import kr.danta.core.state.GameState;import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class Dev091WarServiceTest { private record Fixture(DiplomacyService d,WarService w){} private Fixture f(){GameState g=new GameState();for(String id:new String[]{"a","b","c","d","e"})g.addNation(new NationState(id,id));DiplomacyService d=new DiplomacyService(g);return new Fixture(d,new WarService(g,d));}
 @Test void directAllianceAutoJoinsButDoesNotChain(){var f=f();f.d.setStatus("a","c",DiplomaticStatus.ALLIANCE);f.d.setStatus("c","d",DiplomaticStatus.ALLIANCE);War w=f.w.declareWar("a","b");assertTrue(w.attackers().contains("c"));assertFalse(w.participates("d"));assertEquals(DiplomaticStatus.WAR,f.d.status("c","b"));assertEquals(DiplomaticStatus.ALLIANCE,f.d.status("c","d"));}
 @Test void supportJoinIsExplicitNationParticipation(){var f=f();War w=f.w.declareWar("a","b");f.w.supportJoin(w.warId(),"e",WarSide.DEFENDER);assertEquals(WarSide.DEFENDER,w.sideOf("e"));assertEquals(DiplomaticStatus.WAR,f.d.status("a","e"));}
 @Test void cannotSupportAgainstOwnAlliance(){var f=f();War w=f.w.declareWar("a","b");f.d.setStatus("e","a",DiplomaticStatus.ALLIANCE);assertThrows(IllegalStateException.class,()->f.w.supportJoin(w.warId(),"e",WarSide.DEFENDER));}
}
