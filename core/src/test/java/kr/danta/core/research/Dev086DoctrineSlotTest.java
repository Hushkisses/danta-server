package kr.danta.core.research;

import kr.danta.core.runtime.RuntimeClockService;
import kr.danta.core.runtime.RuntimeScheduler;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class Dev086DoctrineSlotTest {
    @Test void doctrineCapacityStartsAtTwoAndNeverExceedsThree() {
        ResearchState state=new ResearchState("red");
        assertEquals(2,state.doctrineSlots());
        state.setDoctrineSlots(3); assertEquals(3,state.doctrineSlots());
        assertThrows(IllegalArgumentException.class,()->state.setDoctrineSlots(4));
        assertThrows(IllegalArgumentException.class,()->state.setDoctrineSlots(1));
    }
    @Test void nationalSlotsCanMixFieldsFreely() {
        ResearchState state=new ResearchState("red");
        state.selectDoctrine(new DoctrineSelection("iron_wall",ResearchField.MILITARY));
        state.selectDoctrine(new DoctrineSelection("mass_production",ResearchField.INDUSTRY));
        assertEquals(2,state.doctrines().size());
        assertThrows(IllegalStateException.class,()->state.selectDoctrine(new DoctrineSelection("centralization",ResearchField.ADMINISTRATION)));
        state.setDoctrineSlots(3);
        state.selectDoctrine(new DoctrineSelection("centralization",ResearchField.ADMINISTRATION));
        assertEquals(3,state.doctrines().size());
    }
    @Test void doctrineMustBeCompletedTierFourResearch() {
        var d=new ResearchDefinition("military_doctrine","군사 검증 교리",ResearchField.MILITARY,ResearchTier.TIER_4,1,0,Map.of(),List.of(),"iron_wall",null);
        var service=new ResearchService(new RuntimeScheduler(new RuntimeClockService()),Map.of(d.researchId(),d));
        assertThrows(IllegalStateException.class,()->service.selectDoctrine("red",d.researchId()));
        var entry=service.reserve("red",d.researchId()); service.complete(entry.entryId(),"red");
        assertEquals("iron_wall",service.selectDoctrine("red",d.researchId()).doctrineKey());
    }
    @Test void restoredStatePreservesDoctrineCapacityAndSelections() {
        ResearchState restored=ResearchState.restored("red",1,java.util.Set.of(),List.of(),3,List.of(new DoctrineSelection("strategic_magic",ResearchField.MAGIC)));
        assertEquals(3,restored.doctrineSlots()); assertEquals("strategic_magic",restored.doctrines().getFirst().doctrineKey());
    }
}
