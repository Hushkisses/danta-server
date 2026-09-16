package kr.danta.paper.world;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Objects;

/** DEV-MAP-003 deterministic Explorers Guild and wilderness return outpost builder. */
public final class ExplorerGuildBuilder {
    private final DantaWorldService worlds;
    private final DantaWorldConfig config;

    public ExplorerGuildBuilder(DantaWorldService worlds, DantaWorldConfig config) {
        this.worlds = Objects.requireNonNull(worlds, "worlds");
        this.config = Objects.requireNonNull(config, "config");
    }

    public BuildResult buildAll() {
        buildMainGuild();
        buildWildernessReturnOutpost();
        return new BuildResult(config.strategicWorldName(), config.wildernessWorldName());
    }

    public void buildMainGuild() {
        World world = worlds.world(WorldRole.STRATEGIC_MAIN)
                .orElseThrow(() -> new IllegalStateException("strategic world is not loaded"));
        Location spawn = world.getSpawnLocation();
        int cx = spawn.getBlockX(), floorY = spawn.getBlockY() - 1, cz = spawn.getBlockZ();
        loadArea(world, cx, cz, 12);

        // 15x11 expedition hall platform.
        fill(world, cx - 7, floorY, cz - 5, cx + 7, floorY, cz + 5, Material.STONE_BRICKS);
        clear(world, cx - 6, floorY + 1, cz - 4, cx + 6, floorY + 6, cz + 4);
        // timber frame and roof
        pillars(world, cx - 6, cx + 6, floorY + 1, floorY + 5, cz - 4, cz + 4, Material.SPRUCE_LOG);
        fill(world, cx - 7, floorY + 6, cz - 5, cx + 7, floorY + 6, cz + 5, Material.SPRUCE_PLANKS);
        fill(world, cx - 5, floorY + 1, cz - 5, cx + 5, floorY + 4, cz - 5, Material.STONE_BRICKS);
        fill(world, cx - 5, floorY + 1, cz + 5, cx + 5, floorY + 4, cz + 5, Material.STONE_BRICKS);
        // entrances
        clear(world, cx - 1, floorY + 1, cz - 5, cx + 1, floorY + 3, cz - 5);
        clear(world, cx - 1, floorY + 1, cz + 5, cx + 1, floorY + 3, cz + 5);

        // Interaction block is spawn-relative and therefore stable after restart.
        setAtOffset(worlds.relativeLocation(WorldRole.STRATEGIC_MAIN, config.strategicGateOffset()), Material.LODESTONE);
        safePad(worlds.relativeLocation(WorldRole.STRATEGIC_MAIN, config.strategicArrivalOffset()));
        lights(world, cx - 5, cx + 5, floorY + 3, cz - 3, cz + 3, 5);
    }

    public void buildWildernessReturnOutpost() {
        World world = worlds.world(WorldRole.WILDERNESS)
                .orElseThrow(() -> new IllegalStateException("wilderness world is not loaded"));
        Location spawn = world.getSpawnLocation();
        int cx = spawn.getBlockX(), floorY = spawn.getBlockY() - 1, cz = spawn.getBlockZ();
        loadArea(world, cx, cz, 10);

        fill(world, cx - 5, floorY, cz - 4, cx + 5, floorY, cz + 4, Material.COBBLESTONE);
        clear(world, cx - 4, floorY + 1, cz - 3, cx + 4, floorY + 5, cz + 3);
        pillars(world, cx - 4, cx + 4, floorY + 1, floorY + 4, cz - 3, cz + 3, Material.OAK_LOG);
        fill(world, cx - 5, floorY + 5, cz - 4, cx + 5, floorY + 5, cz + 4, Material.OAK_PLANKS);
        setAtOffset(worlds.relativeLocation(WorldRole.WILDERNESS, config.wildernessGateOffset()), Material.LODESTONE);
        safePad(worlds.relativeLocation(WorldRole.WILDERNESS, config.wildernessArrivalOffset()));
        lights(world, cx - 3, cx + 3, floorY + 3, cz - 2, cz + 2, 4);
    }

    private static void safePad(Location center) {
        World world = Objects.requireNonNull(center.getWorld());
        int x = center.getBlockX(), y = center.getBlockY() - 1, z = center.getBlockZ();
        fill(world, x - 1, y, z - 1, x + 1, y, z + 1, Material.SMOOTH_STONE);
        clear(world, x - 1, y + 1, z - 1, x + 1, y + 3, z + 1);
    }

    private static void setAtOffset(Location location, Material material) {
        location.getBlock().setType(material, false);
    }

    private static void lights(World world, int minX, int maxX, int y, int minZ, int maxZ, int step) {
        for (int x = minX; x <= maxX; x += step) for (int z = minZ; z <= maxZ; z += step) {
            Block block = world.getBlockAt(x, y, z);
            if (block.getType().isAir()) block.setType(Material.LIGHT, false);
        }
    }

    private static void pillars(World w, int minX, int maxX, int y0, int y1, int minZ, int maxZ, Material m) {
        fill(w, minX, y0, minZ, minX, y1, minZ, m);
        fill(w, minX, y0, maxZ, minX, y1, maxZ, m);
        fill(w, maxX, y0, minZ, maxX, y1, minZ, m);
        fill(w, maxX, y0, maxZ, maxX, y1, maxZ, m);
    }

    private static void loadArea(World world, int x, int z, int radius) {
        int minCx = Math.floorDiv(x - radius, 16), maxCx = Math.floorDiv(x + radius, 16);
        int minCz = Math.floorDiv(z - radius, 16), maxCz = Math.floorDiv(z + radius, 16);
        for (int cx = minCx; cx <= maxCx; cx++) for (int cz = minCz; cz <= maxCz; cz++) world.getChunkAt(cx, cz).load();
    }

    private static void clear(World w, int x0, int y0, int z0, int x1, int y1, int z1) {
        fill(w, x0, y0, z0, x1, y1, z1, Material.AIR);
    }

    private static void fill(World w, int x0, int y0, int z0, int x1, int y1, int z1, Material m) {
        for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++)
            for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++)
                for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++)
                    w.getBlockAt(x, y, z).setType(m, false);
    }

    public record BuildResult(String strategicWorldName, String wildernessWorldName) {}
}
