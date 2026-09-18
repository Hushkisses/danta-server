package kr.danta.paper.world;

import org.bukkit.entity.EntityType;\nimport org.bukkit.event.entity.CreatureSpawnEvent;\n\nimport java.util.Set;

import java.util.Objects;

/**
 * Strategic mainland spawn policy: only Danta/plugin-managed CUSTOM creature spawns are allowed.
 * Wilderness and other worlds keep normal Bukkit/Paper spawn behavior.
 */
public final class StrategicMobSpawnPolicy {
    private static final Set<String> HOSTILE_TYPES = Set.of(
            "BLAZE", "BOGGED", "BREEZE", "CAVE_SPIDER", "CREAKING", "CREEPER",
            "DROWNED", "ELDER_GUARDIAN", "ENDERMAN", "ENDERMITE", "EVOKER",
            "GHAST", "GUARDIAN", "HOGLIN", "HUSK", "MAGMA_CUBE", "PHANTOM",
            "PIGLIN_BRUTE", "PILLAGER", "RAVAGER", "SHULKER", "SILVERFISH",
            "SKELETON", "SLIME", "SPIDER", "STRAY", "VEX", "VINDICATOR",
            "WARDEN", "WITCH", "WITHER", "WITHER_SKELETON", "ZOGLIN",
            "ZOMBIE", "ZOMBIE_VILLAGER", "ZOMBIFIED_PIGLIN"
    );
    private final String strategicWorldName;

    public StrategicMobSpawnPolicy(String strategicWorldName) {
        this.strategicWorldName = Objects.requireNonNull(strategicWorldName, "strategicWorldName");
    }

    public boolean shouldRemoveExisting(EntityType type) {
        Objects.requireNonNull(type, "type");
        return HOSTILE_TYPES.contains(type.name());
    }

    public boolean shouldBlock(String worldName, CreatureSpawnEvent.SpawnReason reason) {
        Objects.requireNonNull(worldName, "worldName");
        Objects.requireNonNull(reason, "reason");
        return strategicWorldName.equals(worldName)
                && reason != CreatureSpawnEvent.SpawnReason.CUSTOM;
    }
}
