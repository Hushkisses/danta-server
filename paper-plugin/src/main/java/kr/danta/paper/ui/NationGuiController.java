package kr.danta.paper.ui;

import kr.danta.core.nation.NationState;
import kr.danta.core.state.GameState;
import kr.danta.core.territory.StrategicPoint;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** DEV-024 minimal nation GUI and nation selector. */
public final class NationGuiController implements Listener {
    private static final int DETAIL_SIZE = 27;
    private static final int SELECTOR_SIZE = 27;

    private final GameState gameState;

    public NationGuiController(GameState gameState) {
        this.gameState = Objects.requireNonNull(gameState, "gameState");
    }

    /**
     * Opens a nation directly when an id is supplied. Without an id, a single nation is
     * opened immediately; otherwise a selector is shown. This is temporary until player-to-nation
     * membership is introduced by a later gameplay ticket.
     */
    public void open(Player player, String nationId) {
        if (nationId != null && !nationId.isBlank()) {
            NationState nation = gameState.nation(nationId)
                    .orElseThrow(() -> new IllegalArgumentException("nation not found: " + nationId));
            openDetail(player, nation);
            return;
        }

        List<NationState> nations = gameState.nations();
        if (nations.isEmpty()) {
            player.sendMessage(Component.text("생성된 국가가 없습니다.", NamedTextColor.RED));
            return;
        }
        if (nations.size() == 1) {
            openDetail(player, nations.getFirst());
            return;
        }
        openSelector(player, nations);
    }

    private void openSelector(Player player, List<NationState> nations) {
        SelectorHolder holder = new SelectorHolder();
        Inventory inventory = Bukkit.createInventory(holder, SELECTOR_SIZE,
                Component.text("국가 선택", NamedTextColor.GOLD));
        holder.attach(inventory);

        int slot = 0;
        for (NationState nation : nations) {
            if (slot >= SELECTOR_SIZE) break;
            ItemStack item = item(Material.PAPER,
                    Component.text(nation.displayName(), NamedTextColor.YELLOW),
                    List.of(
                            Component.text("국가 ID: " + nation.nationId(), NamedTextColor.GRAY),
                            Component.text("상태: " + UiText.nationStatus(nation.status()), NamedTextColor.WHITE),
                            Component.text("클릭하여 열기", NamedTextColor.GREEN)
                    ));
            inventory.setItem(slot, item);
            holder.nationBySlot.put(slot, nation.nationId());
            slot++;
        }
        player.openInventory(inventory);
    }

    private void openDetail(Player player, NationState nation) {
        NationDetailHolder holder = new NationDetailHolder(nation.nationId());
        Inventory inventory = Bukkit.createInventory(holder, DETAIL_SIZE,
                Component.text("국가 - " + nation.displayName(), NamedTextColor.GOLD));
        holder.attach(inventory);

        inventory.setItem(10, item(Material.PAPER,
                Component.text(nation.displayName(), NamedTextColor.YELLOW),
                List.of(
                        Component.text("국가 ID: " + nation.nationId(), NamedTextColor.GRAY),
                        Component.text("상태: " + UiText.nationStatus(nation.status()), NamedTextColor.WHITE)
                )));

        inventory.setItem(12, item(Material.GOLD_INGOT,
                Component.text("국고", NamedTextColor.GOLD),
                List.of(Component.text(nation.treasury() + " G", NamedTextColor.WHITE))));

        Optional<String> capitalId = nation.capitalPointId();
        List<Component> capitalLore = new ArrayList<>();
        if (capitalId.isEmpty()) {
            capitalLore.add(Component.text("지정되지 않음", NamedTextColor.GRAY));
        } else {
            capitalLore.add(Component.text("거점 ID: " + capitalId.get(), NamedTextColor.WHITE));
            gameState.strategicPoint(capitalId.get()).ifPresentOrElse(point -> {
                capitalLore.add(Component.text(point.displayName(), NamedTextColor.YELLOW));
                capitalLore.add(Component.text(positionText(point), NamedTextColor.GRAY));
            }, () -> capitalLore.add(Component.text("연결된 전략 거점을 찾을 수 없음", NamedTextColor.RED)));
        }
        inventory.setItem(14, item(Material.COMPASS,
                Component.text("수도", NamedTextColor.AQUA), capitalLore));

        long owned = gameState.strategicPoints().stream()
                .filter(point -> point.ownerNationId().filter(nation.nationId()::equals).isPresent())
                .count();
        inventory.setItem(16, item(Material.MAP,
                Component.text("영토", NamedTextColor.GREEN),
                List.of(Component.text("보유 전략 거점: " + owned + "개", NamedTextColor.WHITE))));

        player.openInventory(inventory);
    }

    private static String positionText(StrategicPoint point) {
        var p = point.position();
        return p.worldName() + " " + p.x() + " " + p.y() + " " + p.z();
    }

    private static ItemStack item(Material material, Component name, List<Component> lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(name);
        meta.lore(lore);
        stack.setItemMeta(meta);
        return stack;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (!(holder instanceof SelectorHolder) && !(holder instanceof NationDetailHolder)) return;
        event.setCancelled(true);

        if (!(holder instanceof SelectorHolder selector)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getRawSlot() < 0 || event.getRawSlot() >= event.getView().getTopInventory().getSize()) return;

        String nationId = selector.nationBySlot.get(event.getRawSlot());
        if (nationId == null) return;
        gameState.nation(nationId).ifPresentOrElse(
                nation -> openDetail(player, nation),
                () -> player.sendMessage(Component.text("해당 국가가 더 이상 존재하지 않습니다.", NamedTextColor.RED))
        );
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (holder instanceof SelectorHolder || holder instanceof NationDetailHolder) {
            event.setCancelled(true);
        }
    }

    private static final class SelectorHolder implements InventoryHolder {
        private final Map<Integer, String> nationBySlot = new HashMap<>();
        private Inventory inventory;

        private void attach(Inventory inventory) { this.inventory = inventory; }
        @Override public Inventory getInventory() { return inventory; }
    }

    private static final class NationDetailHolder implements InventoryHolder {
        private final String nationId;
        private Inventory inventory;

        private NationDetailHolder(String nationId) { this.nationId = nationId; }
        private void attach(Inventory inventory) { this.inventory = inventory; }
        @Override public Inventory getInventory() { return inventory; }
        @SuppressWarnings("unused") public String nationId() { return nationId; }
    }
}
