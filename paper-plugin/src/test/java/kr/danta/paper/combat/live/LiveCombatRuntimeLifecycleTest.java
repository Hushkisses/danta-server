package kr.danta.paper.combat.live;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LiveCombatRuntimeLifecycleTest {

    @Test
    void startAndStopUseOneRuntimeSessionAndExposeKoreanStatus() {
        RecordingHooks hooks = new RecordingHooks();
        LiveCombatRuntime runtime = new LiveCombatRuntime(hooks);
        World world = fakeWorld();
        Location origin = new Location(world, 0.0, 64.0, 0.0);

        assertTrue(runtime.startDemo(world, origin));
        assertFalse(runtime.startDemo(world, origin));
        assertEquals("실행 중", runtime.status());
        assertEquals(1, hooks.startCount);

        runtime.tick();
        assertEquals(1, hooks.tickCount);

        runtime.stopDemo();
        assertEquals("정지됨", runtime.status());
        assertEquals(1, hooks.stopCount);
    }

    private static World fakeWorld() {
        return (World) Proxy.newProxyInstance(
                World.class.getClassLoader(),
                new Class<?>[]{World.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getName" -> "dev115-test";
                    case "isChunkLoaded" -> true;
                    case "equals" -> proxy == args[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "toString" -> "FakeWorld";
                    default -> defaultValue(method.getReturnType());
                });
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0.0f;
        if (type == double.class) return 0.0d;
        if (type == char.class) return '\0';
        return null;
    }

    private static final class RecordingHooks implements LiveCombatRuntime.Hooks {
        private int startCount;
        private int tickCount;
        private int stopCount;

        @Override
        public void start(World world, Location origin) {
            startCount++;
        }

        @Override
        public void tick() {
            tickCount++;
        }

        @Override
        public void stop() {
            stopCount++;
        }

        @Override
        public void onTrackedEntityDeath(UUID entityId) {
        }
    }
}
