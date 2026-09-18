package kr.danta.core.army;

import kr.danta.core.combat.TroopType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class Dev119ArmyTroopCompositionTest {

    @Test
    void legacyConstructorMigratesBaseTroopsToInfantry() {
        ArmyState army = new ArmyState("army_a", "red", "red_capital", ArmyStatus.STATIONED, 250L);

        assertEquals(250L, army.troopCount(TroopType.INFANTRY));
        assertEquals(0L, army.troopCount(TroopType.ARCHERS));
        assertEquals(250L, army.totalTroops());
        assertEquals(250L, army.baseTroops());
    }

    @Test
    void compositionConstructorPreservesEachTroopTypeAndTotal() {
        ArmyState army = new ArmyState(
                "army_a", "red", "red_capital", ArmyStatus.STATIONED,
                Map.of(
                        TroopType.INFANTRY, 100L,
                        TroopType.ARCHERS, 40L,
                        TroopType.CAVALRY, 10L));

        assertEquals(100L, army.troopCount(TroopType.INFANTRY));
        assertEquals(40L, army.troopCount(TroopType.ARCHERS));
        assertEquals(10L, army.troopCount(TroopType.CAVALRY));
        assertEquals(150L, army.totalTroops());
        assertEquals(150L, army.baseTroops());
    }

    @Test
    void replacementIsAtomicAndSynchronizesLegacyTotal() {
        ArmyState army = new ArmyState("army_a", "red", "red_capital", ArmyStatus.STATIONED, 100L);

        army.replaceTroopComposition(Map.of(
                TroopType.SPEARMEN, 30L,
                TroopType.MAGIC, 5L));

        assertEquals(0L, army.troopCount(TroopType.INFANTRY));
        assertEquals(30L, army.troopCount(TroopType.SPEARMEN));
        assertEquals(5L, army.troopCount(TroopType.MAGIC));
        assertEquals(35L, army.baseTroops());
    }

    @Test
    void rejectsNegativeCountsWithoutMutatingArmy() {
        ArmyState army = new ArmyState("army_a", "red", "red_capital", ArmyStatus.STATIONED, 100L);

        assertThrows(IllegalArgumentException.class,
                () -> army.replaceTroopComposition(Map.of(TroopType.INFANTRY, -1L)));

        assertEquals(100L, army.troopCount(TroopType.INFANTRY));
        assertEquals(100L, army.baseTroops());
    }

    @Test
    void rejectsTotalOverflowWithoutMutatingArmy() {
        ArmyState army = new ArmyState("army_a", "red", "red_capital", ArmyStatus.STATIONED, 100L);

        assertThrows(ArithmeticException.class,
                () -> army.replaceTroopComposition(Map.of(
                        TroopType.INFANTRY, Long.MAX_VALUE,
                        TroopType.ARCHERS, 1L)));

        assertEquals(100L, army.baseTroops());
    }
}
