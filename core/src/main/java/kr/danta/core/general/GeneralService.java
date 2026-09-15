package kr.danta.core.general;

import kr.danta.core.state.GameState;

import java.util.Objects;

/** DEV-070 creation and ownership validation for generals. */
public final class GeneralService {
    private final GameState gameState;

    public GeneralService(GameState gameState) {
        this.gameState = Objects.requireNonNull(gameState, "gameState");
    }

    public GeneralState create(String generalId, String ownerNationId, GeneralGrade grade, int level) {
        if (!gameState.hasNation(ownerNationId))
            throw new IllegalArgumentException("general owner nation does not exist: " + ownerNationId);
        GeneralState general = new GeneralState(generalId, ownerNationId, grade, level);
        gameState.addGeneral(general);
        return general;
    }
}
