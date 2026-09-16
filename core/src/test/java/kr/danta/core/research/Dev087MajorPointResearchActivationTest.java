package kr.danta.core.research;

import kr.danta.core.nation.NationState;
import kr.danta.core.runtime.RuntimeClockService;
import kr.danta.core.runtime.RuntimeScheduler;
import kr.danta.core.state.GameState;
import kr.danta.core.territory.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class Dev087MajorPointResearchActivationTest {
    @Test void completedHighResearchDeactivatesOnRequiredPointLossAndReactivatesOnRecovery() {
        GameState game=new GameState();
        game.addNation(new NationState("red","적국")); game.addNation(new NationState("blue","청국"));
        StrategicPoint academy=new StrategicPoint("academy","대학도시",StrategicPointType.ACADEMIC,"red",new PointPosition("world",0,70,0),2,Map.of());
        game.addStrategicPoint(academy);
        ResearchDefinition high=new ResearchDefinition("advanced_academy","고급 학술",ResearchField.ADMINISTRATION,ResearchTier.TIER_3,1,0,Map.of(),List.of(),null,"academic");
        ResearchService service=new ResearchService(new RuntimeScheduler(new RuntimeClockService()),Map.of(high.researchId(),high));
        var q=service.reserve("red",high.researchId()); service.complete(q.entryId(),"red");
        assertTrue(service.isEffectActive("red",high.researchId(),game));
        academy.setOwnerNationId("blue");
        assertFalse(service.isEffectActive("red",high.researchId(),game));
        assertEquals(List.of("advanced_academy"),service.inactiveCompletedResearch("red",game));
        academy.setOwnerNationId("red");
        assertTrue(service.isEffectActive("red",high.researchId(),game));
    }
    @Test void ordinaryCompletedResearchRemainsActiveWithoutMajorPointRequirement() {
        GameState game=new GameState(); game.addNation(new NationState("red","적국"));
        ResearchDefinition basic=new ResearchDefinition("basic_admin","기초 행정",ResearchField.ADMINISTRATION,ResearchTier.TIER_1,1,0,Map.of(),List.of(),null,null);
        ResearchService service=new ResearchService(new RuntimeScheduler(new RuntimeClockService()),Map.of(basic.researchId(),basic));
        var q=service.reserve("red",basic.researchId()); service.complete(q.entryId(),"red");
        assertTrue(service.isEffectActive("red",basic.researchId(),game));
    }
}
