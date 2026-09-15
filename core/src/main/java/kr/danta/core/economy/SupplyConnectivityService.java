package kr.danta.core.economy;

import kr.danta.core.nation.NationState;
import kr.danta.core.state.GameState;
import kr.danta.core.territory.StrategicEdge;
import kr.danta.core.territory.StrategicPoint;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/** DEV-054 minimal supply connectivity: own point path to own capital. */
public final class SupplyConnectivityService {
    private final GameState gameState;
    public SupplyConnectivityService(GameState gameState) { this.gameState = Objects.requireNonNull(gameState); }

    public boolean isConnectedToCapital(String nationId, String pointId) {
        NationState nation = gameState.nation(nationId).orElse(null);
        if (nation == null) return false;
        String capital = nation.capitalPointId().orElse(null);
        if (capital == null) return false;
        if (!ownedBy(pointId, nationId) || !ownedBy(capital, nationId)) return false;
        if (pointId.equals(capital)) return true;
        Set<String> visited = new HashSet<>();
        ArrayDeque<String> queue = new ArrayDeque<>();
        visited.add(capital); queue.add(capital);
        while (!queue.isEmpty()) {
            String current = queue.removeFirst();
            for (StrategicEdge edge : gameState.edgesForPoint(current)) {
                String next = edge.otherPoint(current);
                if (!ownedBy(next, nationId) || !visited.add(next)) continue;
                if (next.equals(pointId)) return true;
                queue.addLast(next);
            }
        }
        return false;
    }

    public long totalAmount(String nationId, StrategicResource resource) {
        long total = gameState.strategicResourceStockpile(nationId).map(s -> s.amount(resource)).orElse(0L);
        for (StrategicPoint point : gameState.strategicPoints()) {
            if (!point.ownerNationId().map(nationId::equals).orElse(false)) continue;
            total = Math.addExact(total, gameState.localResourceStockpile(point.pointId())
                    .map(s -> s.amount(resource)).orElse(0L));
        }
        return total;
    }

    public long availableAmount(String nationId, StrategicResource resource) {
        long available = gameState.strategicResourceStockpile(nationId).map(s -> s.amount(resource)).orElse(0L);
        for (StrategicPoint point : gameState.strategicPoints()) {
            if (!point.ownerNationId().map(nationId::equals).orElse(false)) continue;
            if (!isConnectedToCapital(nationId, point.pointId())) continue;
            available = Math.addExact(available, gameState.localResourceStockpile(point.pointId())
                    .map(s -> s.amount(resource)).orElse(0L));
        }
        return available;
    }

    private boolean ownedBy(String pointId, String nationId) {
        return gameState.strategicPoint(pointId).flatMap(StrategicPoint::ownerNationId)
                .map(nationId::equals).orElse(false);
    }
}
