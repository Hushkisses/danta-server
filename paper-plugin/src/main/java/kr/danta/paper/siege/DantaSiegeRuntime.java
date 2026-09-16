package kr.danta.paper.siege;

import kr.danta.core.territory.StrategicPointType;
import kr.danta.paper.map.DevMapDefinition;
import kr.danta.paper.map.DevMapLoader;
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
 * DEV-113 Paper command adapter used to live-verify siege progression before DEV-114+ combat AI
 * supplies real battle results. This listener claims only the exact /danta siege namespace.
 */
public final class DantaSiegeRuntime implements Listener {
    private static final Set<String> BOOTSTRAPPED = ConcurrentHashMap.newKeySet();

    private final JavaPlugin plugin;
    private final PaperSiegeProgressRuntime progress = new PaperSiegeProgressRuntime();
    private DevMapDefinition devMap;
    private String initializationError;

    private DantaSiegeRuntime(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        try {
            this.devMap = new DevMapLoader(plugin).loadDefault();
        } catch (RuntimeException ex) {
            this.initializationError = rootMessage(ex);
            plugin.getLogger().severe("[DEV-113] siege validation runtime initialization failed: " + initializationError);
        }
    }

    public static void bootstrap(JavaPlugin plugin) {
        String key = plugin.getName() + "@" + System.identityHashCode(plugin);
        if (!BOOTSTRAPPED.add(key)) return;
        DantaSiegeRuntime runtime = new DantaSiegeRuntime(plugin);
        plugin.getServer().getPluginManager().registerEvents(runtime, plugin);
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String raw = event.getMessage();
        if (!isSiegeCommand(raw)) return;
        event.setCancelled(true);
        handle(event.getPlayer(), stripLeadingSlash(raw));
    }

    @EventHandler
    public void onServerCommand(ServerCommandEvent event) {
        String raw = event.getCommand();
        if (!isSiegeCommand(raw)) return;
        event.setCancelled(true);
        handle(event.getSender(), stripLeadingSlash(raw));
    }

    private void handle(CommandSender sender, String raw) {
        if (!sender.hasPermission("danta.admin.siege")) {
            sender.sendMessage("§c공성 개발 검증 권한이 없습니다.");
            return;
        }
        if (devMap == null) {
            sender.sendMessage("§c공성 개발 검증 기능을 사용할 수 없습니다. 서버 콘솔을 확인해 주세요.");
            return;
        }

        String[] args = raw.trim().split("\\s+");
        String sub = args.length >= 3 ? args[2].toLowerCase(Locale.ROOT) : "help";
        try {
            switch (sub) {
                case "start" -> {
                    requirePointArg(args, "/danta siege start <거점-id>");
                    StrategicPointType type = pointType(args[3]);
                    progress.start(args[3], type);
                    sender.sendMessage("§a공성 개발 검증을 시작했습니다: §e" + args[3]
                            + " §7(" + pointTypeText(type) + ")");
                    sender.sendMessage("§e" + progress.status(args[3]));
                }
                case "status" -> {
                    requirePointArg(args, "/danta siege status <거점-id>");
                    sender.sendMessage("§e" + progress.status(args[3]));
                }
                case "battle-win" -> {
                    requirePointArg(args, "/danta siege battle-win <거점-id>");
                    progress.recordBattleWin(args[3]);
                    sender.sendMessage("§a전투 승리 결과를 반영했습니다.");
                    sender.sendMessage("§e" + progress.status(args[3]));
                }
                case "gate-breach" -> {
                    requirePointArg(args, "/danta siege gate-breach <거점-id>");
                    progress.recordGateBreach(args[3]);
                    sender.sendMessage("§a성문 파괴 결과를 반영했습니다.");
                    sender.sendMessage("§e" + progress.status(args[3]));
                }
                case "quick-resolve" -> {
                    requirePointArg(args, "/danta siege quick-resolve <거점-id>");
                    progress.recordQuickResolution(args[3]);
                    sender.sendMessage("§a일반 거점의 빠른 점령 결과를 반영했습니다.");
                    sender.sendMessage("§e" + progress.status(args[3]));
                }
                case "clear" -> {
                    requirePointArg(args, "/danta siege clear <거점-id>");
                    progress.clear(args[3]);
                    sender.sendMessage("§a공성 개발 검증 상태를 초기화했습니다: §e" + args[3]);
                }
                default -> sendUsage(sender);
            }
        } catch (RuntimeException ex) {
            plugin.getLogger().warning("[DEV-113] siege validation command failed: " + rootMessage(ex));
            sender.sendMessage("§c공성 요청을 처리하지 못했습니다: " + playerMessage(ex));
        }
    }

    private StrategicPointType pointType(String pointId) {
        return devMap.points().stream()
                .filter(point -> point.id().equals(pointId))
                .map(DevMapDefinition.PointDef::type)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("개발 지도에서 해당 거점을 찾을 수 없습니다: " + pointId));
    }

    private static String pointTypeText(StrategicPointType type) {
        return switch (type) {
            case CAPITAL -> "수도 공성 · 3회 전투";
            case MAJOR -> "중요 거점 · 1회 전투";
            default -> "일반 거점 · 빠른 점령";
        };
    }

    private static void sendUsage(CommandSender sender) {
        sender.sendMessage("§6[DEV-113 공성 검증]");
        sender.sendMessage("§e/danta siege start <거점-id>");
        sender.sendMessage("§e/danta siege status <거점-id>");
        sender.sendMessage("§e/danta siege <battle-win|gate-breach|quick-resolve> <거점-id>");
        sender.sendMessage("§e/danta siege clear <거점-id>");
    }

    private static void requirePointArg(String[] args, String usage) {
        if (args.length < 4) throw new IllegalArgumentException("usage:" + usage);
    }

    private static boolean isSiegeCommand(String raw) {
        if (raw == null) return false;
        String normalized = stripLeadingSlash(raw).trim().toLowerCase(Locale.ROOT);
        return normalized.equals("danta siege") || normalized.startsWith("danta siege ");
    }

    private static String stripLeadingSlash(String raw) {
        return raw.startsWith("/") ? raw.substring(1) : raw;
    }

    private static String playerMessage(RuntimeException error) {
        String message = error.getMessage();
        if (message == null || message.isBlank()) return "알 수 없는 오류가 발생했습니다.";
        if (message.startsWith("usage:")) return "사용법: " + message.substring("usage:".length());
        return message;
    }

    private static String rootMessage(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) current = current.getCause();
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }
}
