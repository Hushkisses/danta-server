package kr.danta.core.combat;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Dev117LogicalForceAiMappingTest {

    @Test
    void developmentDefaultMapsOneAiPerHundredLogicalTroopsWithCeiling() {
        LogicalForceAiMappingPolicy policy = LogicalForceAiMappingPolicy.developmentDefaults();
        Map<TroopType, Long> force = new EnumMap<>(TroopType.class);
        force.put(TroopType.INFANTRY, 250L);
        force.put(TroopType.ARCHERS, 100L);
        force.put(TroopType.MAGIC, 1L);

        LogicalForceAiMappingPolicy.Mapping mapping = policy.map(force);

        assertEquals(100, mapping.logicalTroopsPerAi());
        assertEquals(3, mapping.aiUnits(TroopType.INFANTRY));
        assertEquals(1, mapping.aiUnits(TroopType.ARCHERS));
        assertEquals(1, mapping.aiUnits(TroopType.MAGIC));
        assertEquals(0, mapping.aiUnits(TroopType.CAVALRY));
        assertEquals(5, mapping.totalAiUnits());
    }

    @Test
    void survivorProjectionUsesPerTypeAiSurvivalRatioInsteadOfFlatHundredLosses() {
        LogicalForceAiMappingPolicy policy = new LogicalForceAiMappingPolicy(100);
        Map<TroopType, Long> force = new EnumMap<>(TroopType.class);
        force.put(TroopType.INFANTRY, 250L);
        force.put(TroopType.ARCHERS, 80L);

        LogicalForceAiMappingPolicy.Mapping mapping = policy.map(force);
        Map<TroopType, Integer> survivingAi = new EnumMap<>(TroopType.class);
        survivingAi.put(TroopType.INFANTRY, 2);
        survivingAi.put(TroopType.ARCHERS, 0);

        Map<TroopType, Long> survivors = policy.projectLogicalSurvivors(mapping, survivingAi);

        assertEquals(167L, survivors.get(TroopType.INFANTRY));
        assertEquals(0L, survivors.get(TroopType.ARCHERS));
    }

    @Test
    void allRepresentativesSurvivingPreservesExactLogicalTroopCount() {
        LogicalForceAiMappingPolicy policy = new LogicalForceAiMappingPolicy(100);
        Map<TroopType, Long> force = Map.of(TroopType.CAVALRY, 137L);
        LogicalForceAiMappingPolicy.Mapping mapping = policy.map(force);

        Map<TroopType, Long> survivors = policy.projectLogicalSurvivors(
                mapping, Map.of(TroopType.CAVALRY, 2));

        assertEquals(137L, survivors.get(TroopType.CAVALRY));
    }

    @Test
    void rejectsImpossibleSurvivorCounts() {
        LogicalForceAiMappingPolicy policy = new LogicalForceAiMappingPolicy(100);
        LogicalForceAiMappingPolicy.Mapping mapping = policy.map(Map.of(TroopType.SPEARMEN, 50L));

        assertThrows(IllegalArgumentException.class,
                () -> policy.projectLogicalSurvivors(mapping, Map.of(TroopType.SPEARMEN, 2)));
    }
}
