package kr.danta.paper.combat.ai;

import kr.danta.paper.combat.live.LiveCombatRuntime;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Dev115CombatAiDemoHandlerTest {

    @Test
    void startRequiresPlayerAndLifecycleMessagesAreKorean() {
        RecordingHooks hooks = new RecordingHooks();
        LiveCombatRuntime runtime = new LiveCombatRuntime(hooks);
        CombatAiDemoCommandHandler handler = new CombatAiDemoCommandHandler(runtime);
        List<String> consoleMessages = new ArrayList<>();

        CommandSender console = sender(consoleMessages);
        assertTrue(handler.handle(console, DantaCombatAiCommandBridge.DemoAction.START));
        assertFalse(runtime.status().equals("실행 중"));
        assertTrue(consoleMessages.getLast().contains("게임 안의 플레이어"));

        List<String> playerMessages = new ArrayList<>();
        Player player = player(playerMessages);
        assertTrue(handler.handle(player, DantaCombatAiCommandBridge.DemoAction.START));
        assertEquals("실행 중", runtime.status());
        assertTrue(playerMessages.getLast().contains("전투 시연을 시작"));

        assertTrue(handler.handle(player, DantaCombatAiCommandBridge.DemoAction.STATUS));
        assertTrue(playerMessages.getLast().contains("실행 중"));

        assertTrue(handler.handle(player, DantaCombatAiCommandBridge.DemoAction.STOP));
        assertEquals("정지됨", runtime.status());
        assertTrue(playerMessages.getLast().contains("전투 시연을 종료"));
    }

    private static CommandSender sender(List<String> messages) {
        return (CommandSender) Proxy.newProxyInstance(
                CommandSender.class.getClassLoader(),
                new Class<?>[]{CommandSender.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "sendMessage" -> {
                        if (args != null && args.length > 0 && args[0] instanceof String text) messages.add(text);
                        yield null;
                    }
                    case "getName" -> "Console";
                    case "isPermissionSet", "hasPermission", "isOp" -> true;
                    case "equals" -> proxy == args[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "toString" -> "FakeSender";
                    default -> defaultValue(method.getReturnType());
                });
    }

    private static Player player(List<String> messages) {
        World world = world();
        Location location = new Location(world, 0.0, 64.0, 0.0);
        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "sendMessage" -> {
                        if (args != null && args.length > 0 && args[0] instanceof String text) messages.add(text);
                        yield null;
                    }
                    case "getWorld" -> world;
                    case "getLocation" -> location;
                    case "getUniqueId" -> UUID.randomUUID();
                    case "getName" -> "DevPlayer";
                    case "isPermissionSet", "hasPermission", "isOp", "isOnline" -> true;
                    case "equals" -> proxy == args[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "toString" -> "FakePlayer";
                    default -> defaultValue(method.getReturnType());
                });
    }

    private static World world() {
        return (World) Proxy.newProxyInstance(
                World.class.getClassLoader(),
                new Class<?>[]{World.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getName" -> "dev115-test";
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
        @Override public void start(World world, Location origin) {}
        @Override public void tick() {}
        @Override public void stop() {}
        @Override public void onTrackedEntityDeath(UUID entityId) {}
    }
}
