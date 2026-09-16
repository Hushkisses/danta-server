package kr.danta.paper.map;

import kr.danta.core.territory.StrategicPointType;
import kr.danta.paper.world.DantaWorldRuntime;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class MapStructurePlacer {
    private final JavaPlugin plugin;

    public MapStructurePlacer(JavaPlugin plugin) {
        this.plugin = plugin;
        // DEV-MAP-003 integration hook: the existing map bootstrap already runs during plugin enable.
        // The runtime itself is idempotent and owns only the separate multiworld subsystem.
        DantaWorldRuntime.bootstrap(plugin);
    }

    /**
     * DEV-MAP-002 initial placement pipeline.
     * Explicit admin setup is allowed to load target chunks; normal facility visual sync must not do this.
     */
    public PlacementResult placeAll(DevMapDefinition definition) {
        World world = plugin.getServer().getWorld(definition.worldName());
        if (world == null) {
            return new PlacementResult(0, definition.points().size(),
                    List.of("world not loaded: " + definition.worldName()));
        }

        int placed = 0;
        List<String> failures = new ArrayList<>();
        for (DevMapDefinition.PointDef point : definition.points()) {
            try {
                placeMarker(world, point);
                placed++;
            } catch (RuntimeException ex) {
                failures.add(point.id() + ": " + ex.getMessage());
            }
        }
        return new PlacementResult(placed, failures.size(), List.copyOf(failures));
    }

    private void placeMarker(World world, DevMapDefinition.PointDef point) {
        world.getChunkAt(point.x() >> 4, point.z() >> 4).load();

        int baseY = point.y() - 1;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                world.getBlockAt(point.x() + dx, baseY, point.z() + dz)
                        .setType(Material.STONE_BRICKS, false);
            }
        }

        Block center = world.getBlockAt(point.x(), point.y(), point.z());
        center.setType(markerMaterial(point.type()), false);

        world.getBlockAt(point.x() + 2, point.y(), point.z()).setType(Material.TORCH, false);
        world.getBlockAt(point.x() - 2, point.y(), point.z()).setType(Material.TORCH, false);
        world.getBlockAt(point.x(), point.y(), point.z() + 2).setType(Material.TORCH, false);
        world.getBlockAt(point.x(), point.y(), point.z() - 2).setType(Material.TORCH, false);
    }

    private Material markerMaterial(StrategicPointType type) {
        return switch (type) {
            case CAPITAL -> Material.BEACON;
            case FARM -> Material.HAY_BLOCK;
            case FORESTRY -> Material.OAK_LOG;
            case MINE -> Material.IRON_BLOCK;
            case COMMERCIAL -> Material.EMERALD_BLOCK;
            case ACADEMIC -> Material.BOOKSHELF;
            case BARRACKS -> Material.IRON_BLOCK;
            case PORT -> Material.OAK_PLANKS;
            case GATE -> Material.CHISELED_STONE_BRICKS;
            case MAJOR -> Material.GOLD_BLOCK;
        };
    }

    public record PlacementResult(int placed, int failed, List<String> failures) {}
}
