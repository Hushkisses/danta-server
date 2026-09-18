package kr.danta.paper.world;

import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.Objects;

/**
 * Strategic mainland spawn policy: only Danta/plugin-managed CUSTOM creature spawns are allowed.
 * Wilderness and other worlds keep normal Bukkit/Paper spawn behavior.
 */
public final class StrategicMobSpawnPolicy {
    private final String strategicWorldName;

    public StrategicMobSpawnPolicy(String strategicWorldName) {
        this.strategicWorldName = Objects.requireNonNull(strategicWorldName, "strategicWorldName");
    }

    public boolean shouldBlock(String worldName, CreatureSpawnEvent.SpawnReason reason) {
        Objects.requireNonNull(worldName, "worldName");
        Objects.requireNonNull(reason, "reason");
        return strategicWorldName.equals(worldName)
                && reason != CreatureSpawnEvent.SpawnReason.CUSTOM;
    }
}
