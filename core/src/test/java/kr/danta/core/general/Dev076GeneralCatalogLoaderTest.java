package kr.danta.core.general;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Dev076GeneralCatalogLoaderTest {
    private final GeneralCatalogLoader loader = new GeneralCatalogLoader();

    @Test void loadsStrictDataDrivenDefinition() {
        String yaml = """
                generals:
                  - id: general_alpha
                    displayName: 테스트 장수
                    grade: B
                    level: 3
                    stats:
                      command: 10
                      martial: 20
                      strategy: 30
                      logistics: 40
                    traits: [archer_specialist, steady]
                    abilities: [rally]
                """;
        List<GeneralDefinition> defs = loader.load(stream(yaml));
        assertEquals(1, defs.size());
        GeneralDefinition d = defs.getFirst();
        assertEquals("테스트 장수", d.displayName());
        assertEquals(GeneralGrade.B, d.grade());
        assertEquals(new GeneralStats(10,20,30,40), d.stats());
        assertEquals(List.of("archer_specialist","steady"), d.traitIds());
        assertEquals(List.of("rally"), d.abilityIds());
    }

    @Test void rejectsDuplicateIdsAndUnknownFields() {
        String duplicates = """
                generals:
                  - {id: same, displayName: A, grade: F, level: 1, stats: {command: 0, martial: 0, strategy: 0, logistics: 0}}
                  - {id: same, displayName: B, grade: D, level: 1, stats: {command: 0, martial: 0, strategy: 0, logistics: 0}}
                """;
        assertThrows(IllegalArgumentException.class, () -> loader.load(stream(duplicates)));
        String unknown = """
                generals:
                  - {id: x, displayName: X, grade: F, level: 1, power: 999, stats: {command: 0, martial: 0, strategy: 0, logistics: 0}}
                """;
        assertThrows(IllegalArgumentException.class, () -> loader.load(stream(unknown)));
    }

    @Test void enforcesDesignTraitAbilityCardinality() {
        assertThrows(IllegalArgumentException.class, () -> new GeneralDefinition(
                "x","X",GeneralGrade.F,1,GeneralStats.zero(),
                List.of("a","b","c"), List.of()));
        assertThrows(IllegalArgumentException.class, () -> new GeneralDefinition(
                "x","X",GeneralGrade.F,1,GeneralStats.zero(),
                List.of(), List.of("a","b")));
    }

    @Test void shippedCatalogIsIntentionallyEmptyUntilContentApproval() {
        try (var in = getClass().getResourceAsStream("/generals/initial-generals.yml")) {
            assertNotNull(in);
            assertTrue(loader.load(in).isEmpty());
        } catch (java.io.IOException e) {
            fail(e);
        }
    }

    private static ByteArrayInputStream stream(String value) {
        return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
    }
}
