package kr.danta.core.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Dev041CombatResolverTest {
    private final CombatResolver resolver = new CombatResolver();

    @Test void equalNeutralSameTypeDraws() {
        CombatResult result = resolver.resolve(
                CombatSideInput.neutral("red", TroopType.INFANTRY, 1000),
                CombatSideInput.neutral("blue", TroopType.INFANTRY, 1000));
        assertTrue(result.draw());
        assertEquals(1000.0, result.first().effectivePower(), 0.000001);
    }

    @Test void softCounterAppliesToFavoredTypeOnly() {
        CombatResult result = resolver.resolve(
                CombatSideInput.neutral("red", TroopType.INFANTRY, 1000),
                CombatSideInput.neutral("blue", TroopType.SPEARMEN, 1000));
        assertEquals(1.25, result.first().counterMultiplier(), 0.000001);
        assertEquals(1.0, result.second().counterMultiplier(), 0.000001);
        assertEquals("red", result.winner().orElseThrow());
    }

    @Test void allDeclaredMultipliersParticipate() {
        CombatSideInput boosted = new CombatSideInput("red", TroopType.ARCHERS, 100,
                1.1, 1.2, 1.3, 0.8, 0.9);
        CombatResult result = resolver.resolve(boosted,
                CombatSideInput.neutral("blue", TroopType.CAVALRY, 100));
        assertEquals(100 * 1.1 * 1.2 * 1.3 * 0.8 * 0.9,
                result.first().effectivePower(), 0.000001);
    }
}
