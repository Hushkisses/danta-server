package kr.danta.core.state;

import kr.danta.core.army.ArmyState;
import kr.danta.core.army.ArmyOrder;
import kr.danta.core.combat.GarrisonState;
import kr.danta.core.economy.PersonalWallet;
import kr.danta.core.economy.StrategicResourceStockpile;
import kr.danta.core.economy.LocalResourceStockpile;
import kr.danta.core.nation.NationState;
import kr.danta.core.territory.StrategicPoint;
import kr.danta.core.territory.StrategicEdge;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** In-memory holder for the currently active season and its domain state. */
public final class GameState {
    private SeasonState activeSeason;
    private final Map<String, NationState> nations = new LinkedHashMap<>();
    private final Map<String, StrategicPoint> strategicPoints = new LinkedHashMap<>();
    private final Map<String, StrategicEdge> strategicEdges = new LinkedHashMap<>();
    private final Map<String, ArmyState> armies = new LinkedHashMap<>();
    private final Map<String, ArmyOrder> armyOrdersByArmyId = new LinkedHashMap<>();
    private final Map<String, GarrisonState> garrisonsByPointId = new LinkedHashMap<>();
    private final Map<String, PersonalWallet> personalWalletsByPlayerId = new LinkedHashMap<>();
    private final Map<String, StrategicResourceStockpile> strategicResourcesByNationId = new LinkedHashMap<>();
    private final Map<String, LocalResourceStockpile> localResourcesByPointId = new LinkedHashMap<>();

    public synchronized Optional<SeasonState> activeSeason() { return Optional.ofNullable(activeSeason); }
    public synchronized boolean hasActiveSeason() { return activeSeason != null; }
    public synchronized void activateSeason(SeasonState season) { activeSeason = Objects.requireNonNull(season); }
    public synchronized void clearActiveSeason() { activeSeason = null; }

    public synchronized void addNation(NationState nation) {
        Objects.requireNonNull(nation, "nation");
        if (nations.containsKey(nation.nationId())) {
            throw new IllegalArgumentException("nation already exists: " + nation.nationId());
        }
        nations.put(nation.nationId(), nation);
    }

    public synchronized Optional<NationState> nation(String nationId) {
        return Optional.ofNullable(nations.get(nationId));
    }

    public synchronized List<NationState> nations() {
        return List.copyOf(new ArrayList<>(nations.values()));
    }

    public synchronized boolean hasNation(String nationId) {
        return nations.containsKey(nationId);
    }

    public synchronized void clearNations() {
        nations.clear();
    }

    public synchronized void addStrategicPoint(StrategicPoint point) {
        Objects.requireNonNull(point, "point");
        if (strategicPoints.containsKey(point.pointId())) {
            throw new IllegalArgumentException("strategic point already exists: " + point.pointId());
        }
        strategicPoints.put(point.pointId(), point);
    }

    public synchronized Optional<StrategicPoint> strategicPoint(String pointId) {
        return Optional.ofNullable(strategicPoints.get(pointId));
    }

    public synchronized List<StrategicPoint> strategicPoints() {
        return List.copyOf(new ArrayList<>(strategicPoints.values()));
    }

    public synchronized boolean hasStrategicPoint(String pointId) {
        return strategicPoints.containsKey(pointId);
    }

    public synchronized void clearStrategicPoints() {
        strategicPoints.clear();
    }

    public synchronized void addStrategicEdge(StrategicEdge edge) {
        Objects.requireNonNull(edge, "edge");
        if (strategicEdges.containsKey(edge.edgeId())) {
            throw new IllegalArgumentException("strategic edge already exists: " + edge.edgeId());
        }
        if (!strategicPoints.containsKey(edge.pointAId()) || !strategicPoints.containsKey(edge.pointBId())) {
            throw new IllegalArgumentException("both edge endpoints must exist before adding edge");
        }
        for (StrategicEdge existing : strategicEdges.values()) {
            if (existing.connectsPair(edge.pointAId(), edge.pointBId())) {
                throw new IllegalArgumentException("an edge already connects " + edge.pointAId() + " and " + edge.pointBId());
            }
        }
        strategicEdges.put(edge.edgeId(), edge);
    }

    public synchronized Optional<StrategicEdge> strategicEdge(String edgeId) {
        return Optional.ofNullable(strategicEdges.get(edgeId));
    }

    public synchronized List<StrategicEdge> strategicEdges() {
        return List.copyOf(new ArrayList<>(strategicEdges.values()));
    }

    public synchronized List<StrategicEdge> edgesForPoint(String pointId) {
        List<StrategicEdge> result = new ArrayList<>();
        for (StrategicEdge edge : strategicEdges.values()) {
            if (edge.connects(pointId)) result.add(edge);
        }
        return List.copyOf(result);
    }

    public synchronized boolean hasStrategicEdge(String edgeId) {
        return strategicEdges.containsKey(edgeId);
    }

    public synchronized void clearStrategicEdges() {
        strategicEdges.clear();
    }

    public synchronized void addArmy(ArmyState army) {
        Objects.requireNonNull(army, "army");
        if (armies.containsKey(army.armyId())) {
            throw new IllegalArgumentException("army already exists: " + army.armyId());
        }
        if (!nations.containsKey(army.ownerNationId())) {
            throw new IllegalArgumentException("army owner nation does not exist: " + army.ownerNationId());
        }
        if (!strategicPoints.containsKey(army.locationPointId())) {
            throw new IllegalArgumentException("army location does not exist: " + army.locationPointId());
        }
        armies.put(army.armyId(), army);
    }

    public synchronized Optional<ArmyState> army(String armyId) {
        return Optional.ofNullable(armies.get(armyId));
    }

    public synchronized List<ArmyState> armies() {
        return List.copyOf(new ArrayList<>(armies.values()));
    }

    public synchronized boolean hasArmy(String armyId) {
        return armies.containsKey(armyId);
    }

    public synchronized void clearArmies() {
        armies.clear();
        armyOrdersByArmyId.clear();
    }

    public synchronized void addArmyOrder(ArmyOrder order) {
        Objects.requireNonNull(order, "order");
        if (!armies.containsKey(order.armyId())) {
            throw new IllegalArgumentException("army does not exist: " + order.armyId());
        }
        if (armyOrdersByArmyId.containsKey(order.armyId())) {
            throw new IllegalArgumentException("army already has an order: " + order.armyId());
        }
        armyOrdersByArmyId.put(order.armyId(), order);
    }

    public synchronized Optional<ArmyOrder> armyOrder(String armyId) {
        return Optional.ofNullable(armyOrdersByArmyId.get(armyId));
    }

    public synchronized List<ArmyOrder> armyOrders() {
        return List.copyOf(new ArrayList<>(armyOrdersByArmyId.values()));
    }

    public synchronized Optional<ArmyOrder> removeArmyOrder(String armyId) {
        return Optional.ofNullable(armyOrdersByArmyId.remove(armyId));
    }

    public synchronized void clearArmyOrders() {
        armyOrdersByArmyId.clear();
    }

    public synchronized void addGarrison(GarrisonState garrison) {
        Objects.requireNonNull(garrison, "garrison");
        if (!strategicPoints.containsKey(garrison.pointId())) {
            throw new IllegalArgumentException("garrison point does not exist: " + garrison.pointId());
        }
        if (garrisonsByPointId.containsKey(garrison.pointId())) {
            throw new IllegalArgumentException("garrison already exists: " + garrison.pointId());
        }
        garrisonsByPointId.put(garrison.pointId(), garrison);
    }

    public synchronized Optional<GarrisonState> garrison(String pointId) {
        return Optional.ofNullable(garrisonsByPointId.get(pointId));
    }

    public synchronized List<GarrisonState> garrisons() {
        return List.copyOf(new ArrayList<>(garrisonsByPointId.values()));
    }

    public synchronized boolean hasGarrison(String pointId) {
        return garrisonsByPointId.containsKey(pointId);
    }

    public synchronized Optional<GarrisonState> removeGarrison(String pointId) {
        return Optional.ofNullable(garrisonsByPointId.remove(pointId));
    }

    public synchronized void clearGarrisons() {
        garrisonsByPointId.clear();
    }

    public synchronized void addPersonalWallet(PersonalWallet wallet) {
        Objects.requireNonNull(wallet, "wallet");
        if (personalWalletsByPlayerId.containsKey(wallet.playerId()))
            throw new IllegalArgumentException("personal wallet already exists: " + wallet.playerId());
        personalWalletsByPlayerId.put(wallet.playerId(), wallet);
    }

    public synchronized PersonalWallet getOrCreatePersonalWallet(String playerId) {
        return personalWalletsByPlayerId.computeIfAbsent(playerId, id -> new PersonalWallet(id, 0L));
    }

    public synchronized Optional<PersonalWallet> personalWallet(String playerId) {
        return Optional.ofNullable(personalWalletsByPlayerId.get(playerId));
    }

    public synchronized List<PersonalWallet> personalWallets() {
        return List.copyOf(new ArrayList<>(personalWalletsByPlayerId.values()));
    }

    public synchronized void clearPersonalWallets() {
        personalWalletsByPlayerId.clear();
    }

    public synchronized StrategicResourceStockpile getOrCreateStrategicResourceStockpile(String nationId) {
        if (!nations.containsKey(nationId)) throw new IllegalArgumentException("nation does not exist: " + nationId);
        return strategicResourcesByNationId.computeIfAbsent(nationId, StrategicResourceStockpile::new);
    }

    public synchronized Optional<StrategicResourceStockpile> strategicResourceStockpile(String nationId) {
        return Optional.ofNullable(strategicResourcesByNationId.get(nationId));
    }

    public synchronized List<StrategicResourceStockpile> strategicResourceStockpiles() {
        return List.copyOf(new ArrayList<>(strategicResourcesByNationId.values()));
    }

    public synchronized void addStrategicResourceStockpile(StrategicResourceStockpile stockpile) {
        Objects.requireNonNull(stockpile, "stockpile");
        if (!nations.containsKey(stockpile.nationId()))
            throw new IllegalArgumentException("nation does not exist: " + stockpile.nationId());
        strategicResourcesByNationId.put(stockpile.nationId(), stockpile);
    }

    public synchronized void clearStrategicResourceStockpiles() {
        strategicResourcesByNationId.clear();
    }

    public synchronized LocalResourceStockpile getOrCreateLocalResourceStockpile(String pointId) {
        if (!strategicPoints.containsKey(pointId)) throw new IllegalArgumentException("strategic point does not exist: " + pointId);
        return localResourcesByPointId.computeIfAbsent(pointId, LocalResourceStockpile::new);
    }
    public synchronized Optional<LocalResourceStockpile> localResourceStockpile(String pointId) {
        return Optional.ofNullable(localResourcesByPointId.get(pointId));
    }
    public synchronized List<LocalResourceStockpile> localResourceStockpiles() {
        return List.copyOf(new ArrayList<>(localResourcesByPointId.values()));
    }
    public synchronized void addLocalResourceStockpile(LocalResourceStockpile stockpile) {
        Objects.requireNonNull(stockpile, "stockpile");
        if (!strategicPoints.containsKey(stockpile.pointId()))
            throw new IllegalArgumentException("strategic point does not exist: " + stockpile.pointId());
        localResourcesByPointId.put(stockpile.pointId(), stockpile);
    }
    public synchronized void clearLocalResourceStockpiles() { localResourcesByPointId.clear(); }

}
