package kr.danta.paper.world;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Objects;

/** DEV-MAP-003 right-click interaction listener for the two configured guild lodestones. */
public final class ExplorerGuildListener implements Listener {
    private final ExplorerGuildTravelService travel;

    public ExplorerGuildListener(ExplorerGuildTravelService travel) {
        this.travel = Objects.requireNonNull(travel, "travel");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) return;
        TravelGate gate = null;
        if (travel.isConfiguredGate(event.getClickedBlock(), TravelGate.EXPLORERS_GUILD)) {
            gate = TravelGate.EXPLORERS_GUILD;
        } else if (travel.isConfiguredGate(event.getClickedBlock(), TravelGate.WILDERNESS_RETURN)) {
            gate = TravelGate.WILDERNESS_RETURN;
        }
        if (gate == null) return;

        event.setCancelled(true);
        try {
            ExplorerGuildTravelService.TravelResult result = travel.travel(event.getPlayer(), gate);
            if (result.to() == WorldRole.WILDERNESS) {
                event.getPlayer().sendMessage(Component.text("탐험가 길드를 통해 야생 원정지로 이동했습니다.", NamedTextColor.GREEN));
            } else {
                event.getPlayer().sendMessage(Component.text("야생 원정을 마치고 전략 본토로 귀환했습니다.", NamedTextColor.GREEN));
            }
        } catch (RuntimeException ex) {
            String message = ex.getMessage() == null || ex.getMessage().isBlank()
                    ? "월드 이동 중 문제가 발생했습니다. 관리자에게 알려 주세요." : ex.getMessage();
            event.getPlayer().sendMessage(Component.text(message, NamedTextColor.RED));
        }
    }
}
