package kr.danta.paper.world;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.Objects;

/**
 * DEV-MAP-004 deterministic terrain renderer for the flat strategic mainland.
 * Explicit admin setup only; it never runs automatically on server startup.
 */
public final class StrategicTerrainBuilder {
    private static final int DEV_ORIGIN_X_OFFSET = 192;
    private static final int CLEAR_HEIGHT = 26;

    private final DantaWorldService worlds;

    public StrategicTerrainBuilder(DantaWorldService worlds) {
        this.worlds = Objects.requireNonNull(worlds, "worlds");
    }

    public BuildResult build(TerrainLayout layout) {
        Objects.requireNonNull(layout, "layout");
        World world = worlds.world(WorldRole.STRATEGIC_MAIN)
                .orElseThrow(() -> new IllegalStateException("strategic world is not loaded"));
        Location spawn = world.getSpawnLocation();
        int surfaceY = spawn.getBlockY() - 1;
        int originX = spawn.getBlockX() + DEV_ORIGIN_X_OFFSET;
        int originZ = spawn.getBlockZ();

        for (TerrainTileSpec tile : layout.tiles()) {
            render(world, surfaceY, originX, originZ, layout.tileSize(), tile);
        }
        return new BuildResult(layout.layoutId(), layout.tiles().size(), world.getName(), originX, surfaceY, originZ);
    }

    private void render(World world, int surfaceY, int originX, int originZ, int size, TerrainTileSpec tile) {
        int minX = originX + tile.tileX() * size;
        int minZ = originZ + tile.tileZ() * size;
        int maxX = minX + size - 1;
        int maxZ = minZ + size - 1;
        loadArea(world, minX, maxX, minZ, maxZ);

        // Normalize this development tile without touching the bedrock/deep layers of the flat world.
        fill(world, minX, surfaceY - 2, minZ, maxX, surfaceY - 1, maxZ, Material.DIRT);
        fill(world, minX, surfaceY, minZ, maxX, surfaceY, maxZ, Material.GRASS_BLOCK);
        fill(world, minX, surfaceY + 1, minZ, maxX, surfaceY + CLEAR_HEIGHT, maxZ, Material.AIR);

        switch (tile.type()) {
            case PLAINS -> renderPlains(world, surfaceY, minX, minZ, size);
            case FOREST -> renderForest(world, surfaceY, minX, minZ, size);
            case MOUNTAIN -> renderMountain(world, surfaceY, minX, minZ, size, tile.eastWest());
            case RIVER -> renderRiver(world, surfaceY, minX, minZ, size, tile.eastWest());
            case ROAD -> renderRoad(world, surfaceY, minX, minZ, size, tile.eastWest());
        }
    }

    private void renderPlains(World world, int surfaceY, int minX, int minZ, int size) {
        for (int x = minX + 4; x < minX + size - 4; x += 9) {
            for (int z = minZ + 4; z < minZ + size - 4; z += 9) {
                world.getBlockAt(x, surfaceY + 1, z).setType(Material.SHORT_GRASS, false);
            }
        }
    }

    private void renderForest(World world, int surfaceY, int minX, int minZ, int size) {
        renderPlains(world, surfaceY, minX, minZ, size);
        for (int x = minX + 5; x < minX + size - 4; x += 8) {
            for (int z = minZ + 5; z < minZ + size - 4; z += 8) {
                tree(world, x, surfaceY + 1, z);
            }
        }
    }

    private void renderMountain(World world, int surfaceY, int minX, int minZ, int size, boolean eastWest) {
        int center = size / 2;
        for (int dx = 0; dx < size; dx++) {
            for (int dz = 0; dz < size; dz++) {
                int cross = eastWest ? dz : dx;
                int along = eastWest ? dx : dz;
                int ridge = Math.max(0, 13 - Math.abs(cross - center));
                int variation = ((along / 5) % 3) - 1;
                int height = Math.max(1, ridge + variation);
                int x = minX + dx, z = minZ + dz;
                if (height <= 2) continue;
                for (int y = surfaceY + 1; y < surfaceY + height; y++) {
                    world.getBlockAt(x, y, z).setType(Material.STONE, false);
                }
                world.getBlockAt(x, surfaceY + height, z).setType(
                        height >= 10 ? Material.STONE : Material.GRASS_BLOCK, false);
            }
        }
    }

    private void renderRiver(World world, int surfaceY, int minX, int minZ, int size, boolean eastWest) {
        int center = size / 2;
        int halfWidth = 4;
        for (int i = 0; i < size; i++) {
            int bend = ((i / 8) % 3) - 1;
            for (int cross = center - halfWidth + bend; cross <= center + halfWidth + bend; cross++) {
                int x = eastWest ? minX + i : minX + cross;
                int z = eastWest ? minZ + cross : minZ + i;
                world.getBlockAt(x, surfaceY - 2, z).setType(Material.GRAVEL, false);
                world.getBlockAt(x, surfaceY - 1, z).setType(Material.WATER, false);
                world.getBlockAt(x, surfaceY, z).setType(Material.WATER, false);
            }
        }
    }

    private void renderRoad(World world, int surfaceY, int minX, int minZ, int size, boolean eastWest) {
        int center = size / 2;
        int halfWidth = 2;
        for (int i = 0; i < size; i++) {
            for (int cross = center - halfWidth; cross <= center + halfWidth; cross++) {
                int x = eastWest ? minX + i : minX + cross;
                int z = eastWest ? minZ + cross : minZ + i;
                world.getBlockAt(x, surfaceY, z).setType((i % 7 == 0) ? Material.GRAVEL : Material.COARSE_DIRT, false);
            }
        }
    }

    private void tree(World world, int x, int y, int z) {
        for (int dy = 0; dy < 5; dy++) world.getBlockAt(x, y + dy, z).setType(Material.OAK_LOG, false);
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
            if (Math.abs(dx) + Math.abs(dz) > 3) continue;
            world.getBlockAt(x + dx, y + 4, z + dz).setType(Material.OAK_LEAVES, false);
            if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) {
                world.getBlockAt(x + dx, y + 5, z + dz).setType(Material.OAK_LEAVES, false);
            }
        }
    }

    private void loadArea(World world, int minX, int maxX, int minZ, int maxZ) {
        int minCx = Math.floorDiv(minX, 16), maxCx = Math.floorDiv(maxX, 16);
        int minCz = Math.floorDiv(minZ, 16), maxCz = Math.floorDiv(maxZ, 16);
        for (int cx = minCx; cx <= maxCx; cx++) for (int cz = minCz; cz <= maxCz; cz++) {
            world.getChunkAt(cx, cz).load();
        }
    }

    private void fill(World world, int x0, int y0, int z0, int x1, int y1, int z1, Material material) {
        for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++)
            for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++)
                for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++)
                    world.getBlockAt(x, y, z).setType(material, false);
    }

    public record BuildResult(String layoutId, int tileCount, String worldName,
                              int originX, int surfaceY, int originZ) {}
}
