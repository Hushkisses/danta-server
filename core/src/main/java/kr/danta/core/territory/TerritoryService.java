package kr.danta.core.territory;

import kr.danta.core.event.DomainEventBus;
import kr.danta.core.state.GameState;
import kr.danta.core.territory.event.StrategicPointOwnershipChangedEvent;

import java.util.Objects;

/**
 * Authoritative ownership mutation boundary introduced by DEV-023.
 * Commands/combat should change strategic-point ownership through this service rather than
 * mutating StrategicPoint directly so validation and domain events cannot be skipped.
 */
public final class TerritoryService {
    private final GameState gameState;
    private final DomainEventBus eventBus;

    public TerritoryService(GameState gameState, DomainEventBus eventBus) {
        this.gameState = Objects.requireNonNull(gameState, "gameState");
        this.eventBus = Objects.requireNonNull(eventBus, "eventBus");
    }

    public OwnershipChangeResult changeOwner(String pointId, String newOwnerNationId, String reason) {
        StrategicPoint point = gameState.strategicPoint(pointId)
                .orElseThrow(() -> new IllegalArgumentException("strategic point not found: " + pointId));
        String normalizedNewOwner = normalizeOwner(newOwnerNationId);
        if (normalizedNewOwner != null && !gameState.hasNation(normalizedNewOwner)) {
            throw new IllegalArgumentException("nation not found: " + normalizedNewOwner);
        }

        String previousOwner = point.ownerNationId().orElse(null);
        if (Objects.equals(previousOwner, normalizedNewOwner)) {
            return new OwnershipChangeResult(pointId, previousOwner, normalizedNewOwner, false);
        }

        point.setOwnerNationId(normalizedNewOwner);
        eventBus.publish(new StrategicPointOwnershipChangedEvent(
                pointId,
                previousOwner,
                normalizedNewOwner,
                normalizeReason(reason)));
        return new OwnershipChangeResult(pointId, previousOwner, normalizedNewOwner, true);
    }

    private static String normalizeOwner(String owner) {
        if (owner == null) return null;
        String normalized = owner.trim();
        if (normalized.isEmpty() || normalized.equalsIgnoreCase("none")) return null;
        return normalized;
    }

    private static String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) return "unspecified";
        return reason.trim();
    }

    public record OwnershipChangeResult(
            String pointId,
            String previousOwnerNationId,
            String newOwnerNationId,
            boolean changed
    ) { }
}
