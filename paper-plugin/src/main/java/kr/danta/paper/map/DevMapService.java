package kr.danta.paper.map;

import kr.danta.core.nation.NationState;
import kr.danta.core.state.GameState;
import kr.danta.core.territory.PointPosition;
import kr.danta.core.territory.StrategicEdge;
import kr.danta.core.territory.StrategicPoint;

import java.util.Objects;

public final class DevMapService {
    private final GameState gameState;

    public DevMapService(GameState gameState) {
        this.gameState = Objects.requireNonNull(gameState, "gameState");
    }

    public ImportResult importDefinition(DevMapDefinition definition) {
        int createdNations = 0;
        int existingNations = 0;
        int createdPoints = 0;
        int existingPoints = 0;
        int createdEdges = 0;
        int existingEdges = 0;

        for (DevMapDefinition.NationDef nationDef : definition.nations()) {
            if (gameState.hasNation(nationDef.id())) {
                existingNations++;
            } else {
                gameState.addNation(new NationState(nationDef.id(), nationDef.displayName()));
                createdNations++;
            }
        }

        for (DevMapDefinition.PointDef pointDef : definition.points()) {
            if (gameState.hasStrategicPoint(pointDef.id())) {
                existingPoints++;
                continue;
            }
            if (pointDef.ownerNationId() != null && !gameState.hasNation(pointDef.ownerNationId())) {
                throw new IllegalArgumentException("point owner nation does not exist: "
                        + pointDef.id() + " -> " + pointDef.ownerNationId());
            }

            StrategicPoint point = new StrategicPoint(
                    pointDef.id(),
                    pointDef.displayName(),
                    pointDef.type(),
                    pointDef.ownerNationId(),
                    new PointPosition(definition.worldName(), pointDef.x(), pointDef.y(), pointDef.z()),
                    pointDef.facilitySlots(),
                    pointDef.production());
            gameState.addStrategicPoint(point);
            createdPoints++;
        }

        for (DevMapDefinition.NationDef nationDef : definition.nations()) {
            if (!gameState.hasStrategicPoint(nationDef.capitalPointId())) {
                throw new IllegalArgumentException("capital point does not exist: " + nationDef.capitalPointId());
            }
            gameState.nation(nationDef.id()).orElseThrow().setCapitalPointId(nationDef.capitalPointId());
        }

        for (DevMapDefinition.EdgeDef edgeDef : definition.edges()) {
            if (gameState.hasStrategicEdge(edgeDef.id())) {
                existingEdges++;
                continue;
            }
            StrategicEdge edge = new StrategicEdge(
                    edgeDef.id(),
                    edgeDef.pointAId(),
                    edgeDef.pointBId(),
                    Math.multiplyExact(edgeDef.travelSeconds(), 1000L),
                    edgeDef.tags());
            gameState.addStrategicEdge(edge);
            createdEdges++;
        }

        return new ImportResult(
                createdNations, existingNations,
                createdPoints, existingPoints,
                createdEdges, existingEdges);
    }

    public record ImportResult(
            int createdNations,
            int existingNations,
            int createdPoints,
            int existingPoints,
            int createdEdges,
            int existingEdges
    ) {}
}
