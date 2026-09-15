package kr.danta.core.combat;

import kr.danta.core.state.GameState;

import java.util.Objects;

/** Minimum validation service for creating NPC strategic-point garrisons. */
public final class GarrisonService {
    private final GameState gameState;

    public GarrisonService(GameState gameState) {
        this.gameState = Objects.requireNonNull(gameState, "gameState");
    }

    public GarrisonState createNpcGarrison(String pointId, int troopCount) {
        if (!gameState.hasStrategicPoint(pointId))
            throw new IllegalArgumentException("strategic point does not exist: " + pointId);
        if (gameState.hasGarrison(pointId))
            throw new IllegalArgumentException("garrison already exists: " + pointId);
        GarrisonState garrison = GarrisonState.npc(pointId, troopCount);
        gameState.addGarrison(garrison);
        return garrison;
    }
}
