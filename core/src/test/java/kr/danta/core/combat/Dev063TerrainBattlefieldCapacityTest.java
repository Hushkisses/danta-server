package kr.danta.core.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Dev063TerrainBattlefieldCapacityTest {
    private final BattlefieldCapacityPolicy capacityPolicy = new BattlefieldCapacityPolicy();
    private final TerrainCombatPolicy terrainPolicy = new TerrainCombatPolicy();

    @Test void exposesBroadDesignTerrainCategories() {
        assertArrayEquals(new BattlefieldTerrain[]{
                BattlefieldTerrain.PLAIN, BattlefieldTerrain.FOREST, BattlefieldTerrain.MOUNTAIN,
                BattlefieldTerrain.CANYON, BattlefieldTerrain.COASTAL, BattlefieldTerrain.URBAN_FORTIFIED
        }, BattlefieldTerrain.values());
    }

    @Test void troopsWithinCapacityAreFullyCommitted() {
        CombatSideInput side = CombatSideInput.neutral("red", TroopType.INFANTRY, 800);
        BattlefieldSideResult result = capacityPolicy.evaluate(side,
                new BattlefieldContext(BattlefieldTerrain.PLAIN, 1000));
        assertEquals(800, result.committedTroops());
        assertEquals(0, result.reserveTroops());
        assertEquals(1.0, result.utilization(), 0.000001);
    }

    @Test void troopsAboveCapacityAreExplicitlyHeldAsReserve() {
        CombatSideInput side = CombatSideInput.neutral("red", TroopType.INFANTRY, 1500);
        BattlefieldSideResult result = capacityPolicy.evaluate(side,
                new BattlefieldContext(BattlefieldTerrain.CANYON, 1000));
        assertEquals(1000, result.committedTroops());
        assertEquals(500, result.reserveTroops());
        assertEquals(2.0 / 3.0, result.utilization(), 0.000001);
    }

    @Test void terrainHookUsesConfiguredMultiplierWithoutInventingTerrainTable() {
        BattlefieldContext forest = new BattlefieldContext(BattlefieldTerrain.FOREST, 1000);
        assertEquals(0.9, terrainPolicy.multiplier(forest, 0.9), 0.000001);
        assertEquals(1.15, terrainPolicy.multiplier(forest, 1.15), 0.000001);
    }

    @Test void existingResolverStillAppliesExplicitTerrainMultiplier() {
        CombatSideInput red = new CombatSideInput("red", TroopType.INFANTRY, 1000,
                1.0, 1.0, 1.2, 1.0, 1.0);
        CombatSideInput blue = CombatSideInput.neutral("blue", TroopType.INFANTRY, 1000);
        CombatResult result = new CombatResolverV1().resolve(red, blue).finalResult();
        assertEquals(1200.0, result.first().effectivePower(), 0.000001);
        assertEquals("red", result.winner().orElseThrow());
    }
}
