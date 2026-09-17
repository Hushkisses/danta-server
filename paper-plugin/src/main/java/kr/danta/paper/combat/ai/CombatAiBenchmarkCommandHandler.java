package kr.danta.paper.combat.ai;

import kr.danta.paper.combat.live.CombatSide;
import kr.danta.paper.combat.live.LiveCombatRuntime;
import kr.danta.paper.combat.live.LiveCombatUnit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.Locale;
import java.util.Objects;

/** Paper boundary for DEV-116 live CombatAI benchmark lifecycle and metrics. */
public final class CombatAiBenchmarkCommandHandler {
    private final JavaPlugin plugin;
    private final LiveCombatRuntime runtime;
    private final CombatAiBenchmarkSession session = new CombatAiBenchmarkSession();
    private BukkitTask monitorTask;
    private CombatAiBenchmarkSession.Snapshot lastSummary;

    public CombatAiBenchmarkCommandHandler(JavaPlugin plugin, LiveCombatRuntime runtime) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.runtime = Objects.requireNonNull(runtime, "runtime");
    }

    public boolean handle(CommandSender sender, DantaCombatAiCommandBridge.BenchmarkAction action) {
        Objects.requireNonNull(sender, "sender");
        Objects.requireNonNull(action, "action");

        if (action instanceof DantaCombatAiCommandBridge.BenchmarkAction.Start start) {
            return handleStart(sender, start.unitCount());
        }
        if (action instanceof DantaCombatAiCommandBridge.BenchmarkAction.Stop) {
            stopBenchmark(sender, false);
            return true;
        }
        sender.sendMessage(formatStatus());
        return true;
    }

    private boolean handleStart(CommandSender sender, int unitCount) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cAI 벤치마크 시작은 게임 안의 플레이어만 사용할 수 있습니다.");
            return true;
        }
        if (runtime.active() || session.active()) {
            sender.sendMessage("§c이미 CombatAI 전투 시연 또는 벤치마크가 실행 중입니다.");
            return true;
        }
        if (!runtime.startBenchmark(player.getWorld(), player.getLocation(), unitCount)) {
            sender.sendMessage("§cCombatAI 벤치마크를 시작하지 못했습니다.");
            return true;
        }

        session.start(unitCount, System.currentTimeMillis());
        lastSummary = null;
        monitorTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::sampleAndCheck, 20L, 20L);
        sender.sendMessage("§aCombatAI 벤치마크를 시작했습니다. 실제 AI 병력 " + unitCount + "기를 생성했습니다.");
        sender.sendMessage("§7상태 확인: /danta combat-ai benchmark status");
        return true;
    }

    private void sampleAndCheck() {
        if (!session.active() || !runtime.active()) return;
        Collection<LiveCombatUnit> units = runtime.units();
        double[] tps = plugin.getServer().getTPS();
        session.sample(units.size(), System.currentTimeMillis(), tps.length == 0 ? 20.0 : tps[0],
                plugin.getServer().getAverageTickTime());

        boolean redAlive = units.stream().anyMatch(unit -> unit.side() == CombatSide.RED);
        boolean blueAlive = units.stream().anyMatch(unit -> unit.side() == CombatSide.BLUE);
        if (!redAlive || !blueAlive) {
            lastSummary = session.finish(System.currentTimeMillis());
            runtime.stopDemo();
            cancelMonitor();
            plugin.getLogger().info("[DEV-116] CombatAI benchmark complete: " + plainSummary(lastSummary));
        }
    }

    private void stopBenchmark(CommandSender sender, boolean silent) {
        if (!session.active()) {
            if (!silent) sender.sendMessage("§e실행 중인 CombatAI 벤치마크가 없습니다.");
            return;
        }
        lastSummary = session.finish(System.currentTimeMillis());
        runtime.stopDemo();
        cancelMonitor();
        if (!silent) sender.sendMessage("§aCombatAI 벤치마크를 종료했습니다. " + coloredSummary(lastSummary));
    }

    public void shutdown() {
        if (session.active()) {
            lastSummary = session.finish(System.currentTimeMillis());
        }
        cancelMonitor();
    }

    private String formatStatus() {
        if (session.active()) {
            CombatAiBenchmarkSession.Snapshot snapshot = session.snapshot(System.currentTimeMillis());
            return "§6[CombatAI 벤치마크] §e실행 중 §7| " + coloredSummary(snapshot);
        }
        if (lastSummary != null) {
            return "§6[CombatAI 벤치마크] §e최근 종료 §7| " + coloredSummary(lastSummary);
        }
        return "§6[CombatAI 벤치마크] §e정지됨 §7| 아직 측정 결과가 없습니다.";
    }

    private static String coloredSummary(CombatAiBenchmarkSession.Snapshot snapshot) {
        return String.format(Locale.ROOT,
                "목표=%d, 생존=%d, 경과=%.1f초, 최저 TPS=%.2f, 평균 MSPT=%.2f, 최대 MSPT=%.2f, 표본=%d",
                snapshot.targetUnits(), snapshot.aliveUnits(), snapshot.elapsedMillis() / 1000.0,
                snapshot.minimumTps(), snapshot.averageMspt(), snapshot.maximumMspt(), snapshot.samples());
    }

    private static String plainSummary(CombatAiBenchmarkSession.Snapshot snapshot) {
        return coloredSummary(snapshot).replace("§", "");
    }

    private void cancelMonitor() {
        if (monitorTask != null) {
            monitorTask.cancel();
            monitorTask = null;
        }
    }
}
