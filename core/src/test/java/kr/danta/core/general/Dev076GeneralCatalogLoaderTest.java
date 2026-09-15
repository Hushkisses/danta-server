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
                    grade: A
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
        assertEquals(GeneralGrade.A, d.grade());
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

    @Test void lowerGradesCannotHaveSpecialTraitsOrAbilities() {
        for (GeneralGrade grade : List.of(GeneralGrade.F, GeneralGrade.D, GeneralGrade.C, GeneralGrade.B)) {
            assertThrows(IllegalArgumentException.class, () -> new GeneralDefinition(
                    "x","X",grade,1,GeneralStats.zero(), List.of("special"), List.of()));
            assertThrows(IllegalArgumentException.class, () -> new GeneralDefinition(
                    "x","X",grade,1,GeneralStats.zero(), List.of(), List.of("special")));
        }
    }

    @Test void aAndSGradesMayHaveSpecialTraitsOrAbilities() {
        for (GeneralGrade grade : List.of(GeneralGrade.A, GeneralGrade.S)) {
            assertDoesNotThrow(() -> new GeneralDefinition(
                    "x","X",grade,1,GeneralStats.zero(), List.of("special"), List.of("unique")));
        }
    }

    @Test void enforcesDesignTraitAbilityCardinality() {
        assertThrows(IllegalArgumentException.class, () -> new GeneralDefinition(
                "x","X",GeneralGrade.A,1,GeneralStats.zero(),
                List.of("a","b","c"), List.of()));
        assertThrows(IllegalArgumentException.class, () -> new GeneralDefinition(
                "x","X",GeneralGrade.S,1,GeneralStats.zero(),
                List.of(), List.of("a","b")));
    }

    @Test void shippedCatalogContainsExactlySevenAAndThreeSGenerals() {
        try (var in = getClass().getResourceAsStream("/generals/initial-generals.yml")) {
            assertNotNull(in);
            List<GeneralDefinition> defs = loader.load(in);
            assertEquals(10, defs.size());
            assertEquals(7, defs.stream().filter(d -> d.grade() == GeneralGrade.A).count());
            assertEquals(3, defs.stream().filter(d -> d.grade() == GeneralGrade.S).count());
            assertTrue(defs.stream().allMatch(d -> d.abilityIds().size() == 1));
        } catch (java.io.IOException e) {
            fail(e);
        }
    }

    private static ByteArrayInputStream stream(String value) {
        return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
    }
}
