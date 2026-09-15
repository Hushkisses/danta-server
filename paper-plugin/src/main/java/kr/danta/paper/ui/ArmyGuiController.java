package kr.danta.paper.ui;

import kr.danta.core.army.ArmyOperationQueue;
import kr.danta.core.army.ArmyOrder;
import kr.danta.core.army.ArmyState;
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** DEV-036 minimal read-only army GUI. */
public final class ArmyGuiController implements Listener {
    private static final int SIZE = 54;
    private final GameState gameState;
    private final Map<String, ArmyOperationQueue> operationQueues;

    public ArmyGuiController(GameState gameState, Map<String, ArmyOperationQueue> operationQueues) {
        this.gameState = Objects.requireNonNull(gameState, "gameState");
        this.operationQueues = Objects.requireNonNull(operationQueues, "operationQueues");
    }

    public void open(Player player) {
        List<ArmyState> armies = new ArrayList<>(gameState.armies());
        armies.sort(Comparator.comparing(ArmyState::armyId));
        if (armies.isEmpty()) {
            player.sendMessage(Component.text("생성된 군단이 없습니다.", NamedTextColor.RED));
            return;
        }

        ArmyHolder holder = new ArmyHolder();
        Inventory inventory = Bukkit.createInventory(holder, SIZE, Component.text("군단 현황", NamedTextColor.GOLD));
        holder.attach(inventory);
        int slot = 0;
        for (ArmyState army : armies) {
            if (slot >= 45) break;
            inventory.setItem(slot++, armyItem(army));
        }
        inventory.setItem(49, item(Material.MAP, Component.text("군단 정보", NamedTextColor.GREEN),
                List.of(Component.text("등록 군단: " + armies.size() + "개", NamedTextColor.WHITE),
                        Component.text("현재 군단의 위치·상태·병력·이동 명령을 표시합니다.", NamedTextColor.GRAY))));
        player.openInventory(inventory);
    }

    private ItemStack armyItem(ArmyState army) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("소유국: " + nationName(army.ownerNationId()), NamedTextColor.WHITE));
        lore.add(Component.text("위치: " + pointName(army.locationPointId()), NamedTextColor.WHITE));
        lore.add(Component.text("상태: " + UiText.armyStatus(army.status()), NamedTextColor.YELLOW));
        lore.add(Component.text("기본 병력: " + army.baseTroops(), NamedTextColor.WHITE));

        gameState.armyOrder(army.armyId()).ifPresentOrElse(order -> addOrderLore(lore, order),
                () -> lore.add(Component.text("현재 명령: 없음", NamedTextColor.GRAY)));

        ArmyOperationQueue queue = operationQueues.get(army.armyId());
        if (queue != null) {
            lore.add(Component.text("연속 작전: " + String.join(" → ", queue.destinations()), NamedTextColor.AQUA));
        }
        lore.add(Component.text("군단 ID: " + army.armyId(), NamedTextColor.DARK_GRAY));

        Material material = switch (army.status()) {
            case STATIONED -> Material.SHIELD;
            case MOVING -> Material.COMPASS;
            case IN_BATTLE -> Material.IRON_SWORD;
        };
        return item(material, Component.text(army.armyId(), NamedTextColor.GOLD), lore);
    }

    private void addOrderLore(List<Component> lore, ArmyOrder order) {
        lore.add(Component.empty());
        lore.add(Component.text("현재 명령: " + UiText.armyOrderType(order.type()), NamedTextColor.GREEN));
        lore.add(Component.text("명령 상태: " + UiText.armyOrderStatus(order.status()), NamedTextColor.WHITE));
        lore.add(Component.text("경로: " + pointName(order.route().originPointId()) + " → "
                + pointName(order.route().destinationPointId()), NamedTextColor.GRAY));
    }

    private String nationName(String nationId) {
        return gameState.nation(nationId).map(n -> n.displayName()).orElse(nationId);
    }

    private String pointName(String pointId) {
        return gameState.strategicPoint(pointId).map(StrategicPoint::displayName).orElse(pointId);
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
        if (event.getView().getTopInventory().getHolder() instanceof ArmyHolder) event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof ArmyHolder) event.setCancelled(true);
    }

    private static final class ArmyHolder implements InventoryHolder {
        private Inventory inventory;
        private void attach(Inventory inventory) { this.inventory = inventory; }
        @Override public Inventory getInventory() { return inventory; }
    }
}
