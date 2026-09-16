package kr.danta.paper.combat.live;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

/** DEV-115 live combat runtime lifecycle boundary. */
public final class LiveCombatRuntime {

    public boolean startDemo(World world, Location origin) {
        return false;
    }

    public void stopDemo() {
    }

    public void tick() {
    }

    public String status() {
        return "정지됨";
    }

    public void shutdown() {
        stopDemo();
    }

    public void onTrackedEntityDeath(UUID entityId) {
    }
}
