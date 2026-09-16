package kr.danta.core.score;

import kr.danta.core.state.GameState;
import kr.danta.core.territory.StrategicPoint;
import kr.danta.core.territory.StrategicPointType;

import java.util.Objects;

/**
 * DEV-103 current hegemony score.
 *
 * <p>Unlike accumulated fame, this is derived from the authoritative current world state and is
 * intentionally not persisted as a second mutable score. DEV-104 will supply the complete scoring
 * sources/weights. DEV-103 establishes the recomputable current-state contract only.</p>
 */
public final class HegemonyScoreService {
    private final GameState gameState;
    private final HegemonyScorePolicy policy;

    public HegemonyScoreService(GameState gameState, HegemonyScorePolicy policy) {
        this.gameState = Objects.requireNonNull(gameState, "gameState");
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    public long score(String nationId) {
        requireNation(nationId);
        long total = 0L;
        for (StrategicPoint point : gameState.strategicPoints()) {
            if (!point.ownerNationId().filter(nationId::equals).isPresent()) continue;
            total = Math.addExact(total, policy.strategicPointValue(point.type()));
        }
        return total;
    }

    private void requireNation(String nationId) {
        if (nationId == null || nationId.isBlank() || !gameState.hasNation(nationId)) {
            throw new IllegalArgumentException("nation not found: " + nationId);
        }
    }

    @FunctionalInterface
    public interface HegemonyScorePolicy {
        long strategicPointValue(StrategicPointType type);
    }
}
