package kr.danta.paper.combat.live;

import kr.danta.core.combat.LogicalForceAiMappingPolicy;
import kr.danta.core.combat.TroopType;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LiveCombatDemoFormationTest {

    @Test
    void developmentFormationContainsAllFiveTroopTypesOnBothSides() {
        LiveCombatDemoFormation formation = LiveCombatDemoFormation.developmentDefaults();

        assertEquals(10, formation.slots().size());
        assertEquals(EnumSet.allOf(TroopType.class), formation.troopTypesFor(CombatSide.RED));
        assertEquals(EnumSet.allOf(TroopType.class), formation.troopTypesFor(CombatSide.BLUE));
    }

    @Test
    void mappedFormationUsesLogicalForceRepresentativeCountsPerSide() {
        LogicalForceAiMappingPolicy policy = new LogicalForceAiMappingPolicy(100);
        LiveCombatDemoFormation formation = LiveCombatDemoFormation.fromLogicalForces(
                Map.of(
                        TroopType.INFANTRY, 250L,
                        TroopType.ARCHERS, 80L),
                Map.of(
                        TroopType.SPEARMEN, 101L,
                        TroopType.MAGIC, 1L),
                policy);

        assertEquals(3, formation.count(CombatSide.RED, TroopType.INFANTRY));
        assertEquals(1, formation.count(CombatSide.RED, TroopType.ARCHERS));
        assertEquals(2, formation.count(CombatSide.BLUE, TroopType.SPEARMEN));
        assertEquals(1, formation.count(CombatSide.BLUE, TroopType.MAGIC));
        assertEquals(7, formation.slots().size());
        assertEquals(4, formation.sideUnitCount(CombatSide.RED));
        assertEquals(3, formation.sideUnitCount(CombatSide.BLUE));
    }
}
