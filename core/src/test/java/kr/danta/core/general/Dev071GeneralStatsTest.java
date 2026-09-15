package kr.danta.core.general;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Dev071GeneralStatsTest {
    @Test void storesFourDesignStats() {
        GeneralStats stats = new GeneralStats(12, 9, 15, 7);
        assertEquals(12, stats.command());
        assertEquals(9, stats.martial());
        assertEquals(15, stats.intelligence());
        assertEquals(7, stats.politics());
    }

    @Test void rejectsNegativeStatsButDoesNotInventUnfixedUpperCap() {
        assertThrows(IllegalArgumentException.class, () -> new GeneralStats(-1, 0, 0, 0));
        assertDoesNotThrow(() -> new GeneralStats(1000, 1000, 1000, 1000));
    }

    @Test void generalOwnsAndCanReplaceStats() {
        GeneralState general = new GeneralState("g1", "red", GeneralGrade.C, 3);
        assertEquals(GeneralStats.zero(), general.stats());

        GeneralStats updated = new GeneralStats(10, 20, 30, 40);
        general.setStats(updated);
        assertEquals(updated, general.stats());
    }

    @Test void levelUpDoesNotInventAutomaticStatGrowth() {
        GeneralState general = new GeneralState("g1", "red", GeneralGrade.B, 1);
        general.setStats(new GeneralStats(8, 7, 6, 5));
        general.levelUp();
        assertEquals(2, general.level());
        assertEquals(new GeneralStats(8, 7, 6, 5), general.stats());
    }
}
