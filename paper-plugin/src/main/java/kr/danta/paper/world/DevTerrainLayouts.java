package kr.danta.paper.world;

import java.util.List;

/** DEV-MAP-004 development-only sample layouts; not final season map balance. */
public final class DevTerrainLayouts {
    private DevTerrainLayouts() {}

    public static TerrainLayout sample() {
        return new TerrainLayout("dev-terrain-sample", 32, List.of(
                new TerrainTileSpec(-2, -1, TerrainTileType.MOUNTAIN, 0),
                new TerrainTileSpec(-2, 0, TerrainTileType.MOUNTAIN, 0),
                new TerrainTileSpec(-2, 1, TerrainTileType.MOUNTAIN, 0),
                new TerrainTileSpec(-1, -1, TerrainTileType.FOREST, 0),
                new TerrainTileSpec(-1, 0, TerrainTileType.PLAINS, 0),
                new TerrainTileSpec(-1, 1, TerrainTileType.FOREST, 0),
                new TerrainTileSpec(0, -1, TerrainTileType.ROAD, 0),
                new TerrainTileSpec(0, 0, TerrainTileType.ROAD, 0),
                new TerrainTileSpec(0, 1, TerrainTileType.ROAD, 0),
                new TerrainTileSpec(1, -1, TerrainTileType.PLAINS, 0),
                new TerrainTileSpec(1, 0, TerrainTileType.RIVER, 0),
                new TerrainTileSpec(1, 1, TerrainTileType.PLAINS, 0),
                new TerrainTileSpec(2, -1, TerrainTileType.RIVER, 1),
                new TerrainTileSpec(2, 0, TerrainTileType.RIVER, 1),
                new TerrainTileSpec(2, 1, TerrainTileType.RIVER, 1)
        ));
    }
}
