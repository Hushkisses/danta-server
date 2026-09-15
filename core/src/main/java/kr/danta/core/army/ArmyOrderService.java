package kr.danta.core.army;

import kr.danta.core.state.GameState;
import kr.danta.core.territory.StrategicEdge;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

/** DEV-031 boundary for validating and issuing one-leg movement orders. */
public final class ArmyOrderService {
    private final GameState gameState;
    private final Supplier<String> orderIdSupplier;

    public ArmyOrderService(GameState gameState) {
        this(gameState, () -> UUID.randomUUID().toString());
    }

    public ArmyOrderService(GameState gameState, Supplier<String> orderIdSupplier) {
        this.gameState = Objects.requireNonNull(gameState, "gameState");
        this.orderIdSupplier = Objects.requireNonNull(orderIdSupplier, "orderIdSupplier");
    }

    public ArmyOrder issueMoveOrder(String armyId, String destinationPointId) {
        ArmyState army = gameState.army(armyId)
                .orElseThrow(() -> new IllegalArgumentException("army not found: " + armyId));
        if (army.status() != ArmyStatus.STATIONED) {
            throw new IllegalStateException("army must be STATIONED to receive a move order");
        }
        if (!gameState.hasStrategicPoint(destinationPointId)) {
            throw new IllegalArgumentException("strategic point not found: " + destinationPointId);
        }
        String originPointId = army.locationPointId();
        if (originPointId.equals(destinationPointId)) {
            throw new IllegalArgumentException("army is already at destination: " + destinationPointId);
        }
        StrategicEdge edge = gameState.edgesForPoint(originPointId).stream()
                .filter(candidate -> candidate.connects(destinationPointId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "destination is not adjacent to army location: " + originPointId + " -> " + destinationPointId));

        ArmyOrder order = new ArmyOrder(orderIdSupplier.get(), army.armyId(), ArmyOrderType.MOVE,
                new ArmyRoute(originPointId, destinationPointId, edge.edgeId()), ArmyOrderStatus.PENDING);
        gameState.addArmyOrder(order);
        return order;
    }
}
