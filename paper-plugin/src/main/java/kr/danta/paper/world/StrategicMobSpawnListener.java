package kr.danta.paper.world;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * Keeps the strategic mainland free of ambient creatures while allowing plugin-managed CUSTOM spawns.
 */
public final class StrategicMobSpawnListener implements Listener {
    private final StrategicMobSpawnPolicy policy;

    public StrategicMobSpawnListener(StrategicMobSpawnPolicy policy) {
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        String worldName = event.getLocation().getWorld().getName();
        if (policy.shouldBlock(worldName, event.getSpawnReason())) {
            event.setCancelled(true);
        }
    }

    public int cleanupExisting(World strategicWorld, Logger logger) {
        Objects.requireNonNull(strategicWorld, "strategicWorld");
        Objects.requireNonNull(logger, "logger");
        int removed = 0;
        for (Entity entity : strategicWorld.getEntities()) {
            if (!policy.shouldRemoveExisting(entity.getType())) continue;
            entity.remove();
            removed++;
        }
        if (removed > 0) {
            logger.info("[WORLD] removed " + removed + " existing hostile/slime entities from strategic world");
        }
        return removed;
    }
}
