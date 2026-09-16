package kr.danta.paper.world;

import org.junit.jupiter.api.Test;

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
    void developmentSampleContainsAllBaseTerrainTypes() {
        TerrainLayout layout = DevTerrainLayouts.sample();
        assertEquals(32, layout.tileSize());
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
}
