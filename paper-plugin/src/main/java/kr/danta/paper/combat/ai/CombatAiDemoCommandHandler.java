package kr.danta.paper.combat.ai;

import kr.danta.paper.combat.live.LiveCombatRuntime;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

/** Paper boundary for /danta combat-ai demo lifecycle actions. */
public final class CombatAiDemoCommandHandler {
    private final LiveCombatRuntime runtime;

    public CombatAiDemoCommandHandler(LiveCombatRuntime runtime) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
    }

    public boolean handle(CommandSender sender, DantaCombatAiCommandBridge.DemoAction action) {
        Objects.requireNonNull(sender, "sender");
        Objects.requireNonNull(action, "action");

        return switch (action) {
            case START -> handleStart(sender);
            case STOP -> {
                runtime.stopDemo();
                sender.sendMessage("§aCombatAI 전투 시연을 종료하고 생성된 병력을 정리했습니다.");
                yield true;
            }
            case STATUS -> {
                sender.sendMessage("§6[CombatAI 전투 시연] §e" + runtime.status());
                yield true;
            }
        };
    }

    private boolean handleStart(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c전투 시연 시작은 게임 안의 플레이어만 사용할 수 있습니다.");
            return true;
        }
        if (!runtime.startDemo(player.getWorld(), player.getLocation())) {
            sender.sendMessage("§c이미 CombatAI 전투 시연이 실행 중입니다.");
            return true;
        }
        sender.sendMessage("§aCombatAI 전투 시연을 시작했습니다. 주변에 개발용 병력이 생성됩니다.");
        return true;
    }
}
