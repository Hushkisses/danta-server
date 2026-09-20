package kr.danta.core.siege;

import kr.danta.core.state.GameState;
import java.util.*;

/** DEV-110 registry and invariant boundary for siege instances. */
public final class SiegeService {
    private final GameState gameState;
    private final Map<String, SiegeInstance> byId = new LinkedHashMap<>();

    public SiegeService(GameState gameState) { this.gameState = Objects.requireNonNull(gameState); }

    public synchronized SiegeInstance create(String siegeId, String pointId, String attackerNationId) {
        if (byId.containsKey(siegeId)) throw new IllegalStateException("siege id already exists: " + siegeId);
        var point = gameState.strategicPoint(pointId).orElseThrow(() -> new IllegalArgumentException("point not found: " + pointId));
        String defender = point.ownerNationId().orElseThrow(() -> new IllegalStateException("neutral point cannot be sieged"));
        if (!gameState.hasNation(attackerNationId)) throw new IllegalArgumentException("attacker nation not found: " + attackerNationId);
        if (attackerNationId.equals(defender)) throw new IllegalStateException("nation cannot siege its own point");
        boolean duplicate = byId.values().stream().anyMatch(s -> s.pointId().equals(pointId) && !s.phase().terminal());
        if (duplicate) throw new IllegalStateException("active siege already exists for point: " + pointId);
        SiegeInstance instance = new SiegeInstance(siegeId, pointId, attackerNationId, defender);
        byId.put(siegeId, instance);
        return instance;
    }

    public synchronized SiegeInstance restore(String siegeId, String pointId, String attackerNationId,
                                              String defenderNationId, SiegePhase phase, String result) {
        if (byId.containsKey(siegeId)) throw new IllegalStateException("siege id already exists: " + siegeId);
        if (!gameState.hasStrategicPoint(pointId)) throw new IllegalArgumentException("point not found: " + pointId);
        if (!gameState.hasNation(attackerNationId)) throw new IllegalArgumentException("attacker nation not found: " + attackerNationId);
        if (!gameState.hasNation(defenderNationId)) throw new IllegalArgumentException("defender nation not found: " + defenderNationId);
        boolean duplicate = byId.values().stream().anyMatch(s -> s.pointId().equals(pointId) && !s.phase().terminal());
        if (duplicate && !phase.terminal()) throw new IllegalStateException("active siege already exists for point: " + pointId);
        SiegeInstance restored = SiegeInstance.restored(
                siegeId, pointId, attackerNationId, defenderNationId, phase, result);
        byId.put(siegeId, restored);
        return restored;
    }

    public synchronized Optional<SiegeInstance> find(String siegeId) { return Optional.ofNullable(byId.get(siegeId)); }
    public synchronized List<SiegeInstance> instances() { return List.copyOf(byId.values()); }
    public synchronized void clear() { byId.clear(); }
}
