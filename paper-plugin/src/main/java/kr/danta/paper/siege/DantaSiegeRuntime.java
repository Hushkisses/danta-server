package kr.danta.paper.siege;

import kr.danta.core.siege.SiegeCommanderCasualty;
import kr.danta.core.siege.SiegeCommanderCasualtyPolicy;
import kr.danta.core.siege.SiegeParticipantRegistry;
import kr.danta.core.siege.SiegeSide;
import kr.danta.core.territory.StrategicPointType;
import kr.danta.paper.map.DevMapDefinition;
import kr.danta.paper.map.DevMapLoader;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DEV-113 staged siege validation bridge, extended by DEV-118 for commander elimination.
 * Player participation state is intentionally in-memory until DEV-120 snapshot/recovery.
 */
public final class DantaSiegeRuntime implements Listener {
    private static final Set<String> BOOTSTRAPPED = ConcurrentHashMap.newKeySet();

    private final JavaPlugin plugin;
    private final PaperSiegeProgressRuntime progress = new PaperSiegeProgressRuntime();
    private final SiegeParticipantRegistry participants =
            new SiegeParticipantRegistry(SiegeCommanderCasualtyPolicy.developmentDefaults());
    private final Map<UUID, String> pointByPlayer = new HashMap<>();
    private final Map<UUID, GameMode> originalGameMode = new HashMap<>();

    private DevMapDefinition devMap;
    private String initializationError;

    private DantaSiegeRuntime(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        try {
            this.devMap = new DevMapLoader(plugin).loadDefault();
        } catch (RuntimeException ex) {
            this.initializationError = rootMessage(ex);
            plugin.getLogger().severe("[DEV-113/118] siege validation runtime initialization failed: " + initializationError);
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

    @EventHandler(ignoreCancelled = true)
    public void onParticipantDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        String pointId = pointByPlayer.get(player.getUniqueId());
        if (pointId == null || !progress.active(pointId)) return;
        if (participants.eliminated(pointId, player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        if (!SiegePlayerCommandPolicy.lethal(player.getHealth(), event.getFinalDamage())) return;

        event.setCancelled(true);
        originalGameMode.putIfAbsent(player.getUniqueId(), player.getGameMode());
        SiegeCommanderCasualty casualty = participants.eliminate(pointId, player.getUniqueId());
        player.setGameMode(GameMode.SPECTATOR);
        player.sendMessage("§c[공성] 지휘관이 전투에서 탈락했습니다.");
        player.sendMessage("§e이 공성전이 끝날 때까지 다시 참전할 수 없습니다.");
        player.sendMessage("§7지휘관 보너스 비활성 / 아군 사기 " + casualty.moraleDelta());
        plugin.getLogger().info("[DEV-118] commander eliminated point=" + pointId
                + " player=" + player.getName()
                + " side=" + casualty.side()
                + " moraleDelta=" + casualty.moraleDelta());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String pointId = pointByPlayer.get(player.getUniqueId());
        if (pointId == null) {
            GameMode pendingRestore = originalGameMode.remove(player.getUniqueId());
            if (pendingRestore != null) {
                player.setGameMode(pendingRestore);
                player.sendMessage("§a공성전 종료 상태가 반영되어 원래 게임모드로 복구되었습니다.");
            }
            return;
        }
        boolean active = progress.active(pointId);
        boolean eliminated = participants.eliminated(pointId, player.getUniqueId());
        if (SiegePlayerCommandPolicy.spectatorRequired(active, eliminated)) {
            originalGameMode.putIfAbsent(player.getUniqueId(), player.getGameMode());
            player.setGameMode(GameMode.SPECTATOR);
            player.sendMessage("§e진행 중인 공성전에서 이미 탈락한 상태입니다. 공성 종료까지 재참전할 수 없습니다.");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String pointId = pointByPlayer.get(player.getUniqueId());
        if (pointId == null) return;
        boolean active = progress.active(pointId);
        boolean eliminated = participants.eliminated(pointId, player.getUniqueId());
        if (!SiegePlayerCommandPolicy.spectatorRequired(active, eliminated)) return;
        GameMode previous = originalGameMode.get(player.getUniqueId());
        if (previous != null) player.setGameMode(previous);
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
                    sendPointStatus(sender, args[3]);
                }
                case "status" -> {
                    requirePointArg(args, "/danta siege status <거점-id>");
                    sendPointStatus(sender, args[3]);
                }
                case "battle-win" -> {
                    requirePointArg(args, "/danta siege battle-win <거점-id>");
                    progress.recordBattleWin(args[3]);
                    sender.sendMessage("§a전투 승리 결과를 반영했습니다.");
                    sendPointStatus(sender, args[3]);
                    releaseIfComplete(args[3]);
                }
                case "gate-breach" -> {
                    requirePointArg(args, "/danta siege gate-breach <거점-id>");
                    progress.recordGateBreach(args[3]);
                    sender.sendMessage("§a성문 파괴 결과를 반영했습니다.");
                    sendPointStatus(sender, args[3]);
                }
                case "quick-resolve" -> {
                    requirePointArg(args, "/danta siege quick-resolve <거점-id>");
                    progress.recordQuickResolution(args[3]);
                    sender.sendMessage("§a일반 거점의 빠른 점령 결과를 반영했습니다.");
                    sendPointStatus(sender, args[3]);
                    releaseIfComplete(args[3]);
                }
                case "player" -> handlePlayerCommand(sender, args);
                case "clear" -> {
                    requirePointArg(args, "/danta siege clear <거점-id>");
                    releaseParticipants(args[3]);
                    progress.clear(args[3]);
                    sender.sendMessage("§a공성 개발 검증 상태를 초기화했습니다: §e" + args[3]);
                }
                default -> sendUsage(sender);
            }
        } catch (RuntimeException ex) {
            plugin.getLogger().warning("[DEV-113/118] siege validation command failed: " + rootMessage(ex));
            sender.sendMessage("§c공성 요청을 처리하지 못했습니다: " + playerMessage(ex));
        }
    }

    private void handlePlayerCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            throw new IllegalStateException("공성 플레이어 참가 검증은 플레이어만 사용할 수 있습니다.");
        }
        String action = args.length >= 4 ? args[3].toLowerCase(Locale.ROOT) : "";
        switch (action) {
            case "join" -> {
                if (args.length < 6) {
                    throw new IllegalArgumentException("usage:/danta siege player join <거점-id> <attack|defend>");
                }
                String pointId = args[4];
                if (!progress.active(pointId)) {
                    throw new IllegalStateException("해당 거점에서 진행 중인 공성이 없습니다.");
                }
                String existingPoint = pointByPlayer.get(player.getUniqueId());
                if (existingPoint != null && !existingPoint.equals(pointId) && progress.active(existingPoint)) {
                    throw new IllegalStateException("이미 다른 공성전에 참가 중입니다.");
                }
                SiegeSide side = SiegePlayerCommandPolicy.parseSide(args[5]);
                participants.join(pointId, player.getUniqueId(), side);
                pointByPlayer.put(player.getUniqueId(), pointId);
                sender.sendMessage("§a공성전에 지휘관으로 참가했습니다: §e" + pointId
                        + " §7(" + SiegePlayerCommandPolicy.sideText(side) + "측)");
                sender.sendMessage("§7치명상을 입으면 이 공성전에서는 즉시 탈락하며 재참전할 수 없습니다.");
            }
            case "status" -> {
                if (args.length < 5) {
                    throw new IllegalArgumentException("usage:/danta siege player status <거점-id>");
                }
                String pointId = args[4];
                UUID playerId = player.getUniqueId();
                if (!participants.participating(pointId, playerId)) {
                    sender.sendMessage("§7해당 공성전에 참가 중이 아닙니다.");
                    return;
                }
                SiegeSide side = participants.side(pointId, playerId);
                sender.sendMessage("§6[공성 지휘관 상태]");
                sender.sendMessage("§f거점: §e" + pointId);
                sender.sendMessage("§f측: §e" + SiegePlayerCommandPolicy.sideText(side));
                sender.sendMessage("§f상태: " + (participants.eliminated(pointId, playerId)
                        ? "§c탈락 · 재참전 불가" : "§a전투 가능"));
                sender.sendMessage("§f해당 측 누적 사기 패널티: §e"
                        + participants.moraleDelta(pointId, side));
            }
            default -> throw new IllegalArgumentException(
                    "usage:/danta siege player <join <거점-id> <attack|defend>|status <거점-id>>");
        }
    }

    private void sendPointStatus(CommandSender sender, String pointId) {
        sender.sendMessage("§e" + progress.status(pointId));
        sender.sendMessage("§7지휘관 사기 패널티: 공격 "
                + participants.moraleDelta(pointId, SiegeSide.ATTACKER)
                + " / 방어 " + participants.moraleDelta(pointId, SiegeSide.DEFENDER));
    }

    private void releaseIfComplete(String pointId) {
        if (progress.complete(pointId)) releaseParticipants(pointId);
    }

    private void releaseParticipants(String pointId) {
        for (Map.Entry<UUID, String> entry : Map.copyOf(pointByPlayer).entrySet()) {
            if (!entry.getValue().equals(pointId)) continue;
            UUID playerId = entry.getKey();
            Player player = plugin.getServer().getPlayer(playerId);
            GameMode previous = originalGameMode.get(playerId);
            if (player != null && previous != null) {
                player.setGameMode(previous);
                originalGameMode.remove(playerId);
                player.sendMessage("§a공성전이 종료되어 참가 제한이 해제되었습니다.");
            }
            pointByPlayer.remove(playerId);
        }
        participants.clear(pointId);
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
        sender.sendMessage("§6[공성 검증]");
        sender.sendMessage("§e/danta siege start <거점-id>");
        sender.sendMessage("§e/danta siege status <거점-id>");
        sender.sendMessage("§e/danta siege <battle-win|gate-breach|quick-resolve> <거점-id>");
        sender.sendMessage("§e/danta siege player join <거점-id> <attack|defend>");
        sender.sendMessage("§e/danta siege player status <거점-id>");
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
