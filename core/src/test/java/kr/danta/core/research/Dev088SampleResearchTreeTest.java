package kr.danta.core.research;

import org.junit.jupiter.api.Test;
import java.io.InputStream;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class Dev088SampleResearchTreeTest {
    @Test void sampleTreeLoadsAllFourFieldsAndFiveTiers() {
        InputStream input = Dev088SampleResearchTreeTest.class.getResourceAsStream("/research/dev088-sample-research.yml");
        assertNotNull(input);
        Map<String, ResearchDefinition> defs = new ResearchDefinitionLoader().load(input);
        assertEquals(20, defs.size());
        for (ResearchField field : ResearchField.values()) {
            assertEquals(5, defs.values().stream().filter(d -> d.field() == field).count());
            for (ResearchTier tier : ResearchTier.values())
                assertEquals(1, defs.values().stream().filter(d -> d.field() == field && d.tier() == tier).count());
        }
    }

    @Test void sampleTreeExercisesDoctrineAndMajorPointMetadata() {
        InputStream input = Dev088SampleResearchTreeTest.class.getResourceAsStream("/research/dev088-sample-research.yml");
        assertNotNull(input);
        Map<String, ResearchDefinition> defs = new ResearchDefinitionLoader().load(input);
        assertEquals("iron_wall", defs.get("dev088_military_4").doctrineKey());
        assertEquals("academic", defs.get("dev088_military_3").requiredMajorPointType());
        assertEquals("mine", defs.get("dev088_industry_3").requiredMajorPointType());
        assertEquals("commercial", defs.get("dev088_administration_3").requiredMajorPointType());
        assertEquals("major", defs.get("dev088_magic_3").requiredMajorPointType());
    }
}
