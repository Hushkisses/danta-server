package kr.danta.core.general;

import kr.danta.core.state.GameState;

import java.util.Objects;

/**
 * DEV-076F common acquisition boundary. Content systems (recruitment/raid/event/NPC)
 * call this only after their own eligibility/payment/reward transaction succeeds.
 */
public final class GeneralAcquisitionService {
    private final GameState gameState;

    public GeneralAcquisitionService(GameState gameState) {
        this.gameState = Objects.requireNonNull(gameState, "gameState");
    }

    public GeneralState acquire(GeneralDefinition definition, String nationId) {
        Objects.requireNonNull(definition, "definition");
        if (!gameState.hasNation(nationId)) throw new IllegalArgumentException("nation not found: " + nationId);
        if (gameState.general(definition.id()).isPresent())
            throw new IllegalStateException("general is already owned: " + definition.id());

        GeneralState state = new GeneralState(definition.id(), nationId, definition.grade(), definition.level(), definition.stats());
        definition.traitIds().forEach(id -> state.addTrait(new GeneralTrait(id)));
        definition.abilityIds().forEach(id -> state.addAbility(new GeneralAbility(id)));
        gameState.addGeneral(state);
        return state;
    }
}
