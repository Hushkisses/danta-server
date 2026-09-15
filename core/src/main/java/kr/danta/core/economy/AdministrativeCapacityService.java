package kr.danta.core.economy;

import kr.danta.core.state.GameState;
import kr.danta.core.territory.StrategicPoint;
import kr.danta.core.territory.StrategicPointType;

import java.util.Objects;

/**
 * DEV-057 minimal overextension model.
 * Demand weights follow design v0.3. Exact progressive penalty remains provisional.
 */
public final class AdministrativeCapacityService {
    public static final double BASE_CAPACITY = 6.0;
    private static final double PENALTY_PER_EXCESS_DEMAND = 0.10;
    private static final double MIN_REVENUE_MULTIPLIER = 0.50;

    private final GameState gameState;

    public AdministrativeCapacityService(GameState gameState) {
        this.gameState = Objects.requireNonNull(gameState);
    }

    public Status status(String nationId) {
        if (!gameState.hasNation(nationId)) throw new IllegalArgumentException("nation not found: " + nationId);
        double demand = 0.0;
        int owned = 0;
        for (StrategicPoint point : gameState.strategicPoints()) {
            if (!point.ownerNationId().orElse("").equals(nationId)) continue;
            owned++;
            demand += demandOf(point.type());
        }
        double excess = Math.max(0.0, demand - BASE_CAPACITY);
        double multiplier = Math.max(MIN_REVENUE_MULTIPLIER, 1.0 - excess * PENALTY_PER_EXCESS_DEMAND);
        return new Status(owned, demand, BASE_CAPACITY, excess, multiplier);
    }

    public static double demandOf(StrategicPointType type) {
        return switch (type) {
            case CAPITAL -> 0.0; // v0.3: capital handled separately; v0 excludes it.
            case PORT -> 1.25;
            case MAJOR -> 2.0;
            default -> 1.0;
        };
    }

    public record Status(int ownedPoints, double demand, double capacity, double excess,
                         double revenueMultiplier) {
        public boolean overextended() { return excess > 0.0; }
    }
}
