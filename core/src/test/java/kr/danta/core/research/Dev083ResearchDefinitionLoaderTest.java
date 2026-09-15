package kr.danta.core.research;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class Dev083ResearchDefinitionLoaderTest {
    private final ResearchDefinitionLoader loader = new ResearchDefinitionLoader();

    @Test void loadsDataDrivenDefinitionsWithoutFixingFinalBalanceInCode() {
        String yaml = """
                researches:
                  - id: military_basics
                    name: 군사 기초
                    field: military
                    tier: tier_1
                    durationMinutes: 30
                    goldCost: 500
                  - id: ironwall_doctrine
                    name: 철벽 교리
                    field: military
                    tier: tier_4
                    durationMinutes: 180
                    goldCost: 4000
                    prerequisites: [military_basics]
                    doctrineKey: ironwall
                    requiredMajorPointType: academy
                """;
        var definitions = loader.load(stream(yaml));
        assertEquals(2, definitions.size());
        var doctrine = definitions.get("ironwall_doctrine");
        assertEquals(ResearchField.MILITARY, doctrine.field());
        assertEquals(ResearchTier.TIER_4, doctrine.tier());
        assertEquals(180L * 60_000L, doctrine.durationRuntimeMillis());
        assertEquals("ironwall", doctrine.doctrineKey());
        assertEquals("academy", doctrine.requiredMajorPointType());
    }

    @Test void rejectsDuplicateUnknownAndCyclicPrerequisites() {
        assertThrows(IllegalArgumentException.class, () -> loader.load(stream("""
                researches:
                  - {id: a, name: A, field: industry, tier: tier_1}
                  - {id: a, name: B, field: industry, tier: tier_1}
                """)));
        assertThrows(IllegalArgumentException.class, () -> loader.load(stream("""
                researches:
                  - {id: a, name: A, field: industry, tier: tier_2, prerequisites: [missing]}
                """)));
        assertThrows(IllegalArgumentException.class, () -> loader.load(stream("""
                researches:
                  - {id: a, name: A, field: industry, tier: tier_2, prerequisites: [b]}
                  - {id: b, name: B, field: industry, tier: tier_2, prerequisites: [a]}
                """)));
    }

    @Test void tierFourRequiresDoctrineKey() {
        assertThrows(IllegalArgumentException.class, () -> loader.load(stream("""
                researches:
                  - {id: doctrine, name: 교리, field: administration, tier: tier_4}
                """)));
    }

    private static ByteArrayInputStream stream(String text) {
        return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    }
}
