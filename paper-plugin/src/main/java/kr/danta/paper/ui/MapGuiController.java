package kr.danta.paper.ui;

import kr.danta.core.nation.NationState;
import kr.danta.core.state.GameState;
import kr.danta.core.territory.BattlefieldTag;
import kr.danta.core.territory.StrategicEdge;
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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/** DEV-025 minimal strategic map GUI for the current test graph. */
public final class MapGuiController implements Listener {
    private static final int SIZE = 54;
    private final GameState gameState;

    public MapGuiController(GameState gameState) {
        this.gameState = Objects.requireNonNull(gameState, "gameState");
    }

    public void open(Player player) {
        MapHolder holder = new MapHolder();
        Inventory inventory = Bukkit.createInventory(holder, SIZE,
                Component.text("전략 지도", NamedTextColor.GOLD));
        holder.attach(inventory);

        List<StrategicPoint> points = new ArrayList<>(gameState.strategicPoints());
        points.sort(Comparator.comparing(StrategicPoint::pointId));

        int slot = 0;
        for (StrategicPoint point : points) {
            if (slot >= 45) break;
            inventory.setItem(slot++, pointItem(point));
        }

        inventory.setItem(49, summaryItem(points.size(), gameState.strategicEdges().size()));
        player.openInventory(inventory);
    }

    private ItemStack pointItem(StrategicPoint point) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("유형: " + UiText.strategicPointType(point.type()), NamedTextColor.WHITE));
        lore.add(Component.text("거점 ID: " + point.pointId(), NamedTextColor.DARK_GRAY));
        lore.add(Component.text("소유국: " + ownerText(point), NamedTextColor.WHITE));
        var p = point.position();
        lore.add(Component.text("좌표: " + p.worldName() + " " + p.x() + " " + p.y() + " " + p.z(), NamedTextColor.GRAY));
        lore.add(Component.text("시설 슬롯: " + point.facilitySlots(), NamedTextColor.WHITE));

        List<StrategicEdge> edges = gameState.edgesForPoint(point.pointId());
        lore.add(Component.empty());
        lore.add(Component.text("연결 거점: " + edges.size() + "개", NamedTextColor.AQUA));
        for (StrategicEdge edge : edges.stream().limit(6).toList()) {
            String otherId = edge.otherPoint(point.pointId());
            String otherName = gameState.strategicPoint(otherId).map(StrategicPoint::displayName).orElse(otherId);
            String tags = edge.battlefieldTags().isEmpty() ? "일반로" : edge.battlefieldTags().stream()
                    .map(UiText::battlefieldTag).collect(Collectors.joining(", "));
            lore.add(Component.text("• " + otherName + " · " + formatTravel(edge.baseTravelMillis()) + " · " + tags,
                    NamedTextColor.GRAY));
        }
        if (edges.size() > 6) lore.add(Component.text("외 " + (edges.size() - 6) + "개", NamedTextColor.DARK_GRAY));

        return item(materialFor(point), Component.text(point.displayName(), ownerColor(point)), lore);
    }

    private String ownerText(StrategicPoint point) {
        Optional<String> owner = point.ownerNationId();
        if (owner.isEmpty()) return "중립";
        return gameState.nation(owner.get()).map(NationState::displayName).orElse(owner.get());
    }

    private NamedTextColor ownerColor(StrategicPoint point) {
        return point.ownerNationId().isPresent() ? NamedTextColor.YELLOW : NamedTextColor.GRAY;
    }

    private static Material materialFor(StrategicPoint point) {
        return switch (point.type()) {
            case CAPITAL -> Material.BEACON;
            case FARM -> Material.WHEAT;
            case FORESTRY -> Material.OAK_LOG;
            case MINE -> Material.IRON_ORE;
            case COMMERCIAL -> Material.EMERALD;
            case ACADEMIC -> Material.BOOKSHELF;
            case BARRACKS -> Material.IRON_SWORD;
            case PORT -> Material.OAK_BOAT;
            case GATE -> Material.IRON_DOOR;
            case MAJOR -> Material.NETHER_STAR;
        };
    }

    private ItemStack summaryItem(int pointCount, int edgeCount) {
        return item(Material.MAP, Component.text("지도 정보", NamedTextColor.GREEN), List.of(
                Component.text("전략 거점: " + pointCount + "개", NamedTextColor.WHITE),
                Component.text("연결 간선: " + edgeCount + "개", NamedTextColor.WHITE),
                Component.text("각 거점에 연결된 이동로와 기본 이동시간을 표시합니다.", NamedTextColor.GRAY)
        ));
    }

    private static String formatTravel(long millis) {
        long seconds = Math.max(0L, millis / 1000L);
        long minutes = seconds / 60L;
        long remain = seconds % 60L;
        return minutes > 0 ? minutes + "분 " + remain + "초" : remain + "초";
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
        if (event.getView().getTopInventory().getHolder() instanceof MapHolder) event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof MapHolder) event.setCancelled(true);
    }

    private static final class MapHolder implements InventoryHolder {
        private Inventory inventory;
        private void attach(Inventory inventory) { this.inventory = inventory; }
        @Override public Inventory getInventory() { return inventory; }
    }
}
