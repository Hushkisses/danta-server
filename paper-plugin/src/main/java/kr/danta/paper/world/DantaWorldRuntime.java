package kr.danta.paper.world;

import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DEV-MAP-003 Paper integration boundary.
 *
 * <p>The current Danta command executor is intentionally left untouched while this subsystem is
 * introduced: this listener claims only the exact `/danta world ...` namespace before normal
 * dispatch. The bootstrap is invoked from the existing map setup path and is idempotent.</p>
 */
public final class DantaWorldRuntime implements Listener {
    private static final Set<String> BOOTSTRAPPED = ConcurrentHashMap.newKeySet();

    private final JavaPlugin plugin;
    private final DantaWorldConfig config;
    private DantaWorldService worlds;
    private ExplorerGuildBuilder builder;
    private String initializationError;

    private DantaWorldRuntime(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.config = DantaWorldConfig.defaults();
        try {
            worlds = new DantaWorldService(plugin, config);
            worlds.initialize();
            ExplorerGuildTravelService travel = new ExplorerGuildTravelService(
                    worlds, config, new ExplorerGuildTravelPolicy());
            builder = new ExplorerGuildBuilder(worlds, config);
            plugin.getServer().getPluginManager().registerEvents(new ExplorerGuildListener(travel), plugin);
        } catch (RuntimeException ex) {
            initializationError = rootMessage(ex);
            worlds = null;
            builder = null;
            plugin.getLogger().severe("[DEV-MAP-003] multiworld initialization failed: " + initializationError);
        }
    }

    public static void bootstrap(JavaPlugin plugin) {
        String key = plugin.getName() + "@" + System.identityHashCode(plugin);
        if (!BOOTSTRAPPED.add(key)) return;
        DantaWorldRuntime runtime = new DantaWorldRuntime(plugin);
        plugin.getServer().getPluginManager().registerEvents(runtime, plugin);
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String raw = event.getMessage();
        if (!isWorldCommand(raw)) return;
        event.setCancelled(true);
        handle(event.getPlayer(), stripLeadingSlash(raw));
    }

    @EventHandler
    public void onServerCommand(ServerCommandEvent event) {
        String raw = event.getCommand();
        if (!isWorldCommand(raw)) return;
        event.setCancelled(true);
        handle(event.getSender(), stripLeadingSlash(raw));
    }

    private void handle(CommandSender sender, String raw) {
        String[] args = raw.trim().split("\\s+");
        String sub = args.length >= 3 ? args[2].toLowerCase(Locale.ROOT) : "status";
        if (sub.equals("status")) {
            sendStatus(sender);
            return;
        }
        if (sub.equals("build-guild")) {
            if (!sender.hasPermission("danta.admin.map")) {
                sender.sendMessage("§c탐험가 길드 설치 권한이 없습니다.");
                return;
            }
            if (!available()) {
                sender.sendMessage("§c멀티월드 기능을 사용할 수 없습니다. 서버 콘솔을 확인해 주세요.");
                return;
            }
            try {
                builder.buildAll();
                sender.sendMessage("§a[단타] 탐험가 길드와 야생 귀환 거점을 생성했습니다.");
            } catch (RuntimeException ex) {
                plugin.getLogger().severe("[DEV-MAP-003] guild build failed: " + rootMessage(ex));
                sender.sendMessage("§c탐험가 길드 생성에 실패했습니다. 서버 콘솔을 확인해 주세요.");
            }
            return;
        }
        sender.sendMessage("§e/danta world <status|build-guild>");
    }

    private void sendStatus(CommandSender sender) {
        sender.sendMessage("§6[단타 월드 상태]");
        if (!available()) {
            sender.sendMessage("§c멀티월드: 초기화 실패 - " + initializationError);
            return;
        }
        sender.sendMessage("§f전략 본토: §e" + config.strategicWorldName() + " §7- "
                + (worlds.world(WorldRole.STRATEGIC_MAIN).isPresent() ? "로드됨" : "로드 실패"));
        sender.sendMessage("§f야생: §e" + config.wildernessWorldName() + " §7- "
                + (worlds.world(WorldRole.WILDERNESS).isPresent() ? "로드됨" : "로드 실패"));
        sender.sendMessage("§f자원경제: §a야생 아이템과 국가 전략자원은 분리됨");
        sender.sendMessage("§7야생 월드보더·초기화 주기는 아직 최종 확정되지 않았습니다.");
    }

    private boolean available() {
        return worlds != null && builder != null && initializationError == null;
    }

    private static boolean isWorldCommand(String raw) {
        if (raw == null) return false;
        String normalized = stripLeadingSlash(raw).trim().toLowerCase(Locale.ROOT);
        return normalized.equals("danta world") || normalized.startsWith("danta world ");
    }

    private static String stripLeadingSlash(String raw) {
        return raw.startsWith("/") ? raw.substring(1) : raw;
    }

    private static String rootMessage(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) current = current.getCause();
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }
}
