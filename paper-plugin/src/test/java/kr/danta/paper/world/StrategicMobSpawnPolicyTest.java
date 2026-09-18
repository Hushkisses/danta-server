package kr.danta.paper.world;

import org.bukkit.entity.EntityType;\nimport org.bukkit.event.entity.CreatureSpawnEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StrategicMobSpawnPolicyTest {

    private final StrategicMobSpawnPolicy policy = new StrategicMobSpawnPolicy("danta_main");

    @Test
    void blocksNaturalAndChunkGeneratedCreatureSpawnsInStrategicWorld() {
        assertTrue(policy.shouldBlock("danta_main", CreatureSpawnEvent.SpawnReason.NATURAL));
        assertTrue(policy.shouldBlock("danta_main", CreatureSpawnEvent.SpawnReason.CHUNK_GEN));
        assertTrue(policy.shouldBlock("danta_main", CreatureSpawnEvent.SpawnReason.PATROL));
    }

    @Test
    void allowsPluginCustomSpawnsInStrategicWorld() {
        assertFalse(policy.shouldBlock("danta_main", CreatureSpawnEvent.SpawnReason.CUSTOM));
    }

    @Test
    void doesNotChangeWildernessSpawnBehavior() {
        assertFalse(policy.shouldBlock("danta_wild", CreatureSpawnEvent.SpawnReason.NATURAL));
        assertFalse(policy.shouldBlock("danta_wild", CreatureSpawnEvent.SpawnReason.CHUNK_GEN));
    }

    @Test
    void startupCleanupTargetsHostileMobsAndSlimesButNotPassiveAnimals() {
        assertTrue(policy.shouldRemoveExisting(EntityType.ZOMBIE));
        assertTrue(policy.shouldRemoveExisting(EntityType.SKELETON));
        assertTrue(policy.shouldRemoveExisting(EntityType.SLIME));
        assertFalse(policy.shouldRemoveExisting(EntityType.COW));
        assertFalse(policy.shouldRemoveExisting(EntityType.VILLAGER));
    }
}
