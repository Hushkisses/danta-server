package kr.danta.core.army;

import kr.danta.core.state.GameState;
import kr.danta.core.territory.StrategicEdge;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** DEV-034 validates an explicitly supplied A->B->C... operation route. */
public final class ArmyOperationQueueService {
    private final GameState gameState;

    public ArmyOperationQueueService(GameState gameState) {
        this.gameState = Objects.requireNonNull(gameState, "gameState");
    }

    public ArmyOperationQueue create(String armyId, List<String> destinations) {
        ArmyState army = gameState.army(armyId)
                .orElseThrow(() -> new IllegalArgumentException("army not found: " + armyId));
        if (army.status() != ArmyStatus.STATIONED) {
            throw new IllegalStateException("army must be STATIONED to receive an operation queue");
        }
        if (destinations == null || destinations.isEmpty()) {
            throw new IllegalArgumentException("operation queue must contain at least one destination");
        }

        List<String> normalized = new ArrayList<>();
        String current = army.locationPointId();
        for (String destination : destinations) {
            if (!gameState.hasStrategicPoint(destination)) {
                throw new IllegalArgumentException("strategic point not found: " + destination);
            }
            if (current.equals(destination)) {
                throw new IllegalArgumentException("operation route contains duplicate current point: " + destination);
            }
            final String from = current;
            StrategicEdge edge = gameState.edgesForPoint(from).stream()
                    .filter(candidate -> candidate.connects(destination))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "operation route contains non-adjacent leg: " + from + " -> " + destination));
            normalized.add(destination);
            current = destination;
        }
        return new ArmyOperationQueue(army.armyId(), normalized);
    }
}
