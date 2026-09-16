package kr.danta.core.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Dev040BasicTroopTypesTest {
    @Test void definesExactlyFiveBasicTypes() {
        assertEquals(5, BasicTroopTypes.all().size());
    }

    @Test void followsDesignSoftCounterCycleForOriginalCombatTypes() {
        assertEquals(TroopType.SPEARMEN, BasicTroopTypes.definition(TroopType.INFANTRY).strongAgainst());
        assertEquals(TroopType.CAVALRY, BasicTroopTypes.definition(TroopType.SPEARMEN).strongAgainst());
        assertEquals(TroopType.ARCHERS, BasicTroopTypes.definition(TroopType.CAVALRY).strongAgainst());
        assertEquals(TroopType.INFANTRY, BasicTroopTypes.definition(TroopType.ARCHERS).strongAgainst());
        for (TroopType type : new TroopType[]{TroopType.INFANTRY, TroopType.SPEARMEN, TroopType.ARCHERS, TroopType.CAVALRY}) {
            assertEquals(1.25, BasicTroopTypes.definition(type).strongAgainstMultiplier(), 0.000001);
        }
    }

    @Test void rolesMatchBasicDesign() {
        assertEquals(TroopRole.FRONTLINE, BasicTroopTypes.definition(TroopType.INFANTRY).role());
        assertEquals(TroopRole.FRONTLINE, BasicTroopTypes.definition(TroopType.SPEARMEN).role());
        assertEquals(TroopRole.RANGED, BasicTroopTypes.definition(TroopType.ARCHERS).role());
        assertEquals(TroopRole.MOBILE, BasicTroopTypes.definition(TroopType.CAVALRY).role());
    }

    @Test void magicTroopIsSharedSupportType() {
        TroopTypeDefinition magic = BasicTroopTypes.definition(TroopType.MAGIC);
        assertEquals(TroopRole.SUPPORT, magic.role());
        assertEquals(1.0, magic.basePower(), 0.000001);
    }
}
