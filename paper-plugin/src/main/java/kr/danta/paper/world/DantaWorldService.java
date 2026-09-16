package kr.danta.paper.world;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

/** DEV-MAP-003 creates/loads the two gameplay worlds and resolves their stable spawn-relative anchors. */
public final class DantaWorldService {
    private final JavaPlugin plugin;
    private final DantaWorldConfig config;
    private final Logger logger;
    private World strategicMain;
    private World wilderness;

    public DantaWorldService(JavaPlugin plugin, DantaWorldConfig config) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.config = Objects.requireNonNull(config, "config");
        this.logger = plugin.getLogger();
    }

    public void initialize() {
        strategicMain = loadOrCreate(config.strategicWorldName(), WorldType.FLAT);
        wilderness = loadOrCreate(config.wildernessWorldName(), WorldType.NORMAL);
        logger.info("[DEV-MAP-003] worlds ready: strategic=" + strategicMain.getName()
                + ", wilderness=" + wilderness.getName());
    }

    public Optional<World> world(WorldRole role) {
        if (role == null) return Optional.empty();
        return Optional.ofNullable(role == WorldRole.STRATEGIC_MAIN ? strategicMain : wilderness);
    }

    public Optional<WorldRole> roleOf(World world) {
        if (world == null) return Optional.empty();
        if (strategicMain != null && strategicMain.getUID().equals(world.getUID())) return Optional.of(WorldRole.STRATEGIC_MAIN);
        if (wilderness != null && wilderness.getUID().equals(world.getUID())) return Optional.of(WorldRole.WILDERNESS);
        return Optional.empty();
    }

    public Location relativeLocation(WorldRole role, DantaWorldConfig.RelativePoint offset) {
        World world = world(role).orElseThrow(() -> new IllegalStateException("world is not loaded: " + role));
        Location spawn = world.getSpawnLocation();
        return new Location(world,
                spawn.getBlockX() + offset.x() + 0.5,
                spawn.getBlockY() + offset.y(),
                spawn.getBlockZ() + offset.z() + 0.5,
                0.0f, 0.0f);
    }

    private World loadOrCreate(String name, WorldType type) {
        World loaded = plugin.getServer().getWorld(name);
        if (loaded != null) return loaded;
        World created = new WorldCreator(name)
                .environment(World.Environment.NORMAL)
                .type(type)
                .createWorld();
        if (created == null) throw new IllegalStateException("world could not be created: " + name);
        return created;
    }
}
