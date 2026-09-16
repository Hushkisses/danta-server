package kr.danta.paper.world;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TerrainLayoutTest {
    @Test
    void rejectsInvalidRotation() {
        assertThrows(IllegalArgumentException.class,
                () -> new TerrainTileSpec(0, 0, TerrainTileType.ROAD, 4));
    }

    @Test
    void rejectsDuplicateCoordinates() {
        assertThrows(IllegalArgumentException.class, () -> new TerrainLayout("duplicate", 32, List.of(
                new TerrainTileSpec(0, 0, TerrainTileType.PLAINS, 0),
                new TerrainTileSpec(0, 0, TerrainTileType.FOREST, 0)
        )));
    }

    @Test
    void developmentSampleUsesThreeByThreeChunkTiles() {
        TerrainLayout layout = DevTerrainLayouts.sample();
        assertEquals(48, layout.tileSize());
    }

    @Test
    void developmentSampleContainsAllBaseTerrainTypes() {
        TerrainLayout layout = DevTerrainLayouts.sample();
        for (TerrainTileType type : TerrainTileType.values()) {
            assertTrue(layout.count(type) > 0, "missing tile type: " + type);
        }
    }

    @Test
    void quarterTurnChangesDirectionalAxis() {
        assertFalse(new TerrainTileSpec(0, 0, TerrainTileType.RIVER, 0).eastWest());
        assertTrue(new TerrainTileSpec(0, 0, TerrainTileType.RIVER, 1).eastWest());
        assertFalse(new TerrainTileSpec(0, 0, TerrainTileType.RIVER, 2).eastWest());
        assertTrue(new TerrainTileSpec(0, 0, TerrainTileType.RIVER, 3).eastWest());
    }

    @Test
    void terrainBlendPolicyKeepsSharedEdgeOffsetsDeterministicAndBounded() {
        assertDoesNotThrow(() -> {
            Class<?> policy = Class.forName("kr.danta.paper.world.TerrainBlendPolicy");
            Method edgeOffset = policy.getMethod("edgeOffset", int.class, int.class, int.class, boolean.class);
            Method spill = policy.getMethod("spillDistance", int.class, int.class, TerrainTileType.class);

            int eastOfLeft = (int) edgeOffset.invoke(null, 0, 0, 11, true);
            int westOfRight = (int) edgeOffset.invoke(null, 1, 0, 11, false);
            assertEquals(eastOfLeft, westOfRight, "shared tile edge must use the same deterministic offset");
            assertTrue(Math.abs(eastOfLeft) <= 3, "edge jitter must stay visually subtle");

            int forestSpill = (int) spill.invoke(null, 0, 0, TerrainTileType.FOREST);
            int plainsSpill = (int) spill.invoke(null, 0, 0, TerrainTileType.PLAINS);
            assertTrue(forestSpill >= 2 && forestSpill <= 5, "forest must overlap its nominal tile edge");
            assertEquals(0, plainsSpill, "plains must remain the neutral base layer");
        });
    }
}
