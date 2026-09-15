package kr.danta.core.army;

import kr.danta.core.state.GameState;
import kr.danta.core.territory.BattlefieldTag;
import kr.danta.core.territory.StrategicEdge;

import java.time.Duration;
import java.util.Objects;

/** DEV-032 movement-time calculator. Scheduling and arrival belong to DEV-033. */
public final class ArmyMovementTimeService {
    private final GameState gameState;

    public ArmyMovementTimeService(GameState gameState) {
        this.gameState = Objects.requireNonNull(gameState, "gameState");
    }

    public ArmyMovementTime calculateForArmy(String armyId) {
        ArmyState army = gameState.army(armyId)
                .orElseThrow(() -> new IllegalArgumentException("army not found: " + armyId));
        ArmyOrder order = gameState.armyOrder(armyId)
                .orElseThrow(() -> new IllegalArgumentException("army has no current order: " + armyId));
        return calculate(army, order);
    }

    public ArmyMovementTime calculate(ArmyState army, ArmyOrder order) {
        Objects.requireNonNull(army, "army");
        Objects.requireNonNull(order, "order");
        if (!army.armyId().equals(order.armyId())) {
            throw new IllegalArgumentException("order belongs to another army: " + order.orderId());
        }
        if (order.type() != ArmyOrderType.MOVE) {
            throw new IllegalArgumentException("order is not a movement order: " + order.orderId());
        }
        if (!army.locationPointId().equals(order.route().originPointId())) {
            throw new IllegalStateException("army location no longer matches order origin: " + army.armyId());
        }

        StrategicEdge edge = gameState.strategicEdge(order.route().edgeId())
                .orElseThrow(() -> new IllegalArgumentException("strategic edge not found: " + order.route().edgeId()));
        if (!edge.connectsPair(order.route().originPointId(), order.route().destinationPointId())) {
            throw new IllegalStateException("movement order route no longer matches strategic edge: " + order.orderId());
        }

        double multiplier = 1.0D;
        for (BattlefieldTag tag : edge.battlefieldTags()) {
            multiplier *= tagMultiplier(tag);
        }

        long baseMillis = edge.baseTravelMillis();
        long effectiveMillis = Math.max(1L, (long) Math.ceil(baseMillis * multiplier));
        return new ArmyMovementTime(
                Duration.ofMillis(baseMillis),
                Duration.ofMillis(effectiveMillis),
                multiplier,
                edge.battlefieldTags());
    }

    static double tagMultiplier(BattlefieldTag tag) {
        return switch (tag) {
            case ROAD -> 0.90D;
            case MOUNTAIN_PASS -> 1.20D;
            case FOREST_PATH -> 1.10D;
            case CANYON -> 1.15D;
            case SEA_ROUTE -> 0.90D;
            case LANDING_ROUTE -> 1.25D;
            case PLAIN, COASTAL -> 1.00D;
        };
    }
}
