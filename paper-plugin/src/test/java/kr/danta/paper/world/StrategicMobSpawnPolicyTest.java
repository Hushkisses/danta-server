package kr.danta.paper.world;

import org.bukkit.event.entity.CreatureSpawnEvent;
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
}
