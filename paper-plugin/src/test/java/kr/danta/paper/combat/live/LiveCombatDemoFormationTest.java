package kr.danta.paper.combat.live;

import kr.danta.core.combat.LogicalForceAiMappingPolicy;\nimport kr.danta.core.combat.TroopType;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;\nimport java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LiveCombatDemoFormationTest {

    @Test
    void developmentFormationContainsAllFiveTroopTypesOnBothSides() {
        LiveCombatDemoFormation formation = LiveCombatDemoFormation.developmentDefaults();

        assertEquals(10, formation.slots().size());
        assertEquals(EnumSet.allOf(TroopType.class), formation.troopTypesFor(CombatSide.RED));
        assertEquals(EnumSet.allOf(TroopType.class), formation.troopTypesFor(CombatSide.BLUE));
    }
}
