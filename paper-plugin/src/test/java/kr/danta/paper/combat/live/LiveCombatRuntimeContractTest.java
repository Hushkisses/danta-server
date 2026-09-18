package kr.danta.paper.combat.live;

import kr.danta.core.combat.LogicalForceAiMappingPolicy;\nimport kr.danta.core.combat.TroopType;\nimport org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class LiveCombatRuntimeContractTest {

    @Test
    void exposesApprovedDev115LifecycleApi() throws Exception {
        Class<?> runtime = Class.forName("kr.danta.paper.combat.live.LiveCombatRuntime");

        assertNotNull(runtime.getMethod("startDemo", World.class, Location.class));
        assertNotNull(runtime.getMethod(
                "startMappedBattle",
                World.class,
                Location.class,
                java.util.Map.class,
                java.util.Map.class,
                LogicalForceAiMappingPolicy.class));
        assertNotNull(runtime.getMethod("stopDemo"));
        assertNotNull(runtime.getMethod("tick"));
        assertNotNull(runtime.getMethod("status"));
        assertNotNull(runtime.getMethod("shutdown"));
        assertNotNull(runtime.getMethod("onTrackedEntityDeath", java.util.UUID.class));
    }
}
