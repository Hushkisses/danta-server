package kr.danta.paper.combat.ai;

import kr.danta.paper.combat.live.LiveCombatListener;
import kr.danta.paper.combat.live.LiveCombatRuntime;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DEV-115 Paper integration boundary for the live CombatAI demo.
 *
 * <p>This follows the existing world/siege integration pattern: the runtime claims only the exact
 * {@code /danta combat-ai demo ...} namespace before the legacy Danta command executor receives it.
 * Existing DEV-114 {@code profiles}/{@code decide} validation commands remain untouched.</p>
 */
public final class DantaCombatAiRuntime implements Listener {
    private static final Set<String> BOOTSTRAPPED = ConcurrentHashMap.newKeySet();

    private final JavaPlugin plugin;
    private final String bootstrapKey;
    private final DantaCombatAiCommandBridge bridge = new DantaCombatAiCommandBridge();
    private final LiveCombatRuntime liveRuntime;
    private final CombatAiDemoCommandHandler demoHandler;

    private DantaCombatAiRuntime(JavaPlugin plugin, String bootstrapKey) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.bootstrapKey = Objects.requireNonNull(bootstrapKey, "bootstrapKey");
        this.liveRuntime = new LiveCombatRuntime(plugin, new PaperCombatAiRuntime());
        this.demoHandler = new CombatAiDemoCommandHandler(liveRuntime);
        plugin.getServer().getPluginManager().registerEvents(new LiveCombatListener(liveRuntime), plugin);
    }

    public static void bootstrap(JavaPlugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        String key = plugin.getName() + "@" + System.identityHashCode(plugin);
        if (!BOOTSTRAPPED.add(key)) return;

        try {
            DantaCombatAiRuntime runtime = new DantaCombatAiRuntime(plugin, key);
            plugin.getServer().getPluginManager().registerEvents(runtime, plugin);
        } catch (RuntimeException ex) {
            BOOTSTRAPPED.remove(key);
            throw ex;
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String raw = event.getMessage();
        if (!isDemoCommand(raw)) return;
        event.setCancelled(true);
        handle(event.getPlayer(), stripLeadingSlash(raw));
    }

    @EventHandler
    public void onServerCommand(ServerCommandEvent event) {
        String raw = event.getCommand();
        if (!isDemoCommand(raw)) return;
        event.setCancelled(true);
        handle(event.getSender(), stripLeadingSlash(raw));
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin() != plugin) return;
        liveRuntime.shutdown();
        BOOTSTRAPPED.remove(bootstrapKey);
    }

    private void handle(CommandSender sender, String raw) {
        if (!sender.hasPermission("danta.admin.army")) {
            sender.sendMessage("§c전투 AI 검증 권한이 없습니다.");
            return;
        }

        String[] tokens = raw.trim().split("\\s+");
        String[] dantaArgs = tokens.length <= 1
                ? new String[0]
                : Arrays.copyOfRange(tokens, 1, tokens.length);

        bridge.parseDemoAction(dantaArgs).ifPresentOrElse(
                action -> {
                    try {
                        demoHandler.handle(sender, action);
                    } catch (RuntimeException ex) {
                        plugin.getLogger().warning("[DEV-115] CombatAI demo command failed: " + rootMessage(ex));
                        sender.sendMessage("§cCombatAI 전투 시연 요청을 처리하지 못했습니다. 서버 콘솔을 확인해 주세요.");
                    }
                },
                () -> sender.sendMessage("§e/danta combat-ai demo <start|stop|status>"));
    }

    static boolean isDemoCommand(String raw) {
        if (raw == null) return false;
        String normalized = stripLeadingSlash(raw).trim().toLowerCase(Locale.ROOT);
        return normalized.equals("danta combat-ai demo")
                || normalized.startsWith("danta combat-ai demo ");
    }

    private static String stripLeadingSlash(String raw) {
        return raw.startsWith("/") ? raw.substring(1) : raw;
    }

    private static String rootMessage(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) current = current.getCause();
        String message = current.getMessage();
        return current.getClass().getSimpleName() + (message == null ? "" : ": " + message);
    }
}
