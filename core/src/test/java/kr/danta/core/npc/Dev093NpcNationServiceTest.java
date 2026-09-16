package kr.danta.core.npc;
import kr.danta.core.nation.NationState;import kr.danta.core.state.GameState;import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class Dev093NpcNationServiceTest {
 @Test void npcControlIsExplicitAndUnique(){GameState g=new GameState();g.addNation(new NationState("red","red"));NpcNationService s=new NpcNationService(g);assertEquals(StrategicAiPhase.IDLE,s.register("red").phase());assertTrue(s.isNpc("red"));assertThrows(IllegalStateException.class,()->s.register("red"));s.unregister("red");assertFalse(s.isNpc("red"));}
 @Test void strategicAiLifecycleIsGuarded(){NpcNationState s=new NpcNationState("npc");s.beginEvaluation();assertEquals(StrategicAiPhase.EVALUATING,s.phase());s.decide("hold");assertEquals(StrategicAiPhase.DECIDED,s.phase());s.beginExecution();s.finishExecution();assertEquals(StrategicAiPhase.IDLE,s.phase());assertNull(s.decisionKey());}
 @Test void invalidTransitionIsRejected(){NpcNationState s=new NpcNationState("npc");assertThrows(IllegalStateException.class,s::beginExecution);}
}
