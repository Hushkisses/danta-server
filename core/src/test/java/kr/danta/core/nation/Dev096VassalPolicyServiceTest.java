package kr.danta.core.nation;
import kr.danta.core.state.GameState;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class Dev096VassalPolicyServiceTest {
 @Test void tributeTransfersOnlyNationTreasuryRevenue(){GameState g=new GameState();NationState a=new NationState("red","Red");NationState b=new NationState("blue","Blue");g.addNation(a);g.addNation(b);VassalService v=new VassalService(g);v.restore(new VassalRelation("red","blue",1));VassalPolicyService p=new VassalPolicyService(g,v,20);a.deposit(1000);assertEquals(200,p.transferGoldRevenueTribute("red",1000));assertEquals(800,a.treasury());assertEquals(200,b.treasury());}
 @Test void restrictionsAndPassage(){GameState g=new GameState();g.addNation(new NationState("red","Red"));g.addNation(new NationState("blue","Blue"));VassalService v=new VassalService(g);v.restore(new VassalRelation("red","blue",1));VassalPolicyService p=new VassalPolicyService(g,v);assertFalse(p.canFormAlliance("red"));assertFalse(p.canSupportJoin("red"));assertFalse(p.canDeclareWar("red","blue"));assertTrue(p.hasOverlordPassage("blue","red"));}
}