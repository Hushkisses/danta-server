package kr.danta.core.research;

import kr.danta.core.runtime.RuntimeClockService;
import kr.danta.core.runtime.RuntimeScheduler;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class Dev085ResearchEligibilityTest {
    private static ResearchDefinition def(String id, ResearchField field, ResearchTier tier, List<String> prerequisites) {
        return new ResearchDefinition(id,id,field,tier,1,0,Map.of(),prerequisites,
                tier == ResearchTier.TIER_4 ? "test_doctrine" : null,null);
    }
    @Test void allFourFieldsRemainDistinctAndQueryable() {
        var service=service();
        assertEquals("military_1",service.definitions(ResearchField.MILITARY).getFirst().researchId());
        assertEquals("industry_1",service.definitions(ResearchField.INDUSTRY).getFirst().researchId());
        assertEquals("administration_1",service.definitions(ResearchField.ADMINISTRATION).getFirst().researchId());
        assertEquals("magic_1",service.definitions(ResearchField.MAGIC).getFirst().researchId());
    }
    @Test void prerequisiteBlocksReservationUntilCompleted() {
        var service=service();
        assertEquals(ResearchService.Eligibility.MISSING_PREREQUISITES,service.eligibility("red","military_2"));
        assertEquals(List.of("military_1"),service.missingPrerequisites("red","military_2"));
        assertThrows(IllegalStateException.class,()->service.reserve("red","military_2"));
        var first=service.reserve("red","military_1");
        service.complete(first.entryId(),"red");
        assertEquals(ResearchService.Eligibility.ELIGIBLE,service.eligibility("red","military_2"));
        assertEquals("military_2",service.reserve("red","military_2").researchId());
    }
    @Test void queuedResearchDoesNotCountAsCompletedPrerequisite() {
        var service=service(); service.reserve("red","military_1");
        assertEquals(ResearchService.Eligibility.MISSING_PREREQUISITES,service.eligibility("red","military_2"));
    }
    private static ResearchService service(){
        Map<String,ResearchDefinition> defs=Map.of(
                "military_1",def("military_1",ResearchField.MILITARY,ResearchTier.TIER_1,List.of()),
                "military_2",def("military_2",ResearchField.MILITARY,ResearchTier.TIER_2,List.of("military_1")),
                "industry_1",def("industry_1",ResearchField.INDUSTRY,ResearchTier.TIER_1,List.of()),
                "administration_1",def("administration_1",ResearchField.ADMINISTRATION,ResearchTier.TIER_1,List.of()),
                "magic_1",def("magic_1",ResearchField.MAGIC,ResearchTier.TIER_1,List.of()));
        return new ResearchService(new RuntimeScheduler(new RuntimeClockService()),defs);
    }
}
