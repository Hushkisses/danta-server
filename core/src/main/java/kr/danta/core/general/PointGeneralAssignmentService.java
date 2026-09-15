package kr.danta.core.general;

import kr.danta.core.state.GameState;
import kr.danta.core.territory.StrategicPoint;

import java.util.Objects;

/**
 * General assignment to a strategic point for civil/point effects.
 * One point may host at most one assigned general.
 * Army command and point assignment are mutually exclusive.
 */
public final class PointGeneralAssignmentService {
    private final GameState gameState;

    public PointGeneralAssignmentService(GameState gameState) {
        this.gameState = Objects.requireNonNull(gameState, "gameState");
    }

    public void assign(String generalId, String pointId) {
        GeneralState general = requireGeneral(generalId);
        StrategicPoint point = requirePoint(pointId);
        String owner = point.ownerNationId()
                .orElseThrow(() -> new IllegalStateException("strategic point has no owner"));
        if (!general.ownerNationId().equals(owner))
            throw new IllegalArgumentException("general and strategic point must belong to the same nation");
        if (general.isCaptive())
            throw new IllegalStateException("captive general cannot be assigned to a strategic point");
        if (gameState.commandedArmyId(generalId).isPresent())
            throw new IllegalStateException("army commander cannot also be assigned to a strategic point");
        if (gameState.assignedPointId(generalId).isPresent())
            throw new IllegalStateException("general is already assigned to a strategic point");
        if (point.assignedGeneralId().isPresent())
            throw new IllegalStateException("strategic point already has an assigned general");
        point.setAssignedGeneralId(generalId);
    }

    public void move(String generalId, String destinationPointId) {
        GeneralState general = requireGeneral(generalId);
        String sourcePointId = gameState.assignedPointId(generalId)
                .orElseThrow(() -> new IllegalStateException("general is not assigned to a strategic point"));
        if (sourcePointId.equals(destinationPointId)) return;
        StrategicPoint destination = requirePoint(destinationPointId);
        String owner = destination.ownerNationId()
                .orElseThrow(() -> new IllegalStateException("destination strategic point has no owner"));
        if (!general.ownerNationId().equals(owner))
            throw new IllegalArgumentException("general and destination strategic point must belong to the same nation");
        if (destination.assignedGeneralId().isPresent())
            throw new IllegalStateException("destination strategic point already has an assigned general");

        requirePoint(sourcePointId).clearAssignedGeneralId();
        destination.setAssignedGeneralId(generalId);
    }

    public void unassign(String generalId) {
        String pointId = gameState.assignedPointId(generalId)
                .orElseThrow(() -> new IllegalStateException("general is not assigned to a strategic point"));
        requirePoint(pointId).clearAssignedGeneralId();
    }

    private GeneralState requireGeneral(String id) {
        return gameState.general(id).orElseThrow(() -> new IllegalArgumentException("general not found: " + id));
    }

    private StrategicPoint requirePoint(String id) {
        return gameState.strategicPoint(id)
                .orElseThrow(() -> new IllegalArgumentException("strategic point not found: " + id));
    }
}
