package kr.danta.core.general;

import kr.danta.core.army.ArmyState;
import kr.danta.core.army.ArmyStatus;
import kr.danta.core.state.GameState;

import java.util.Objects;

/** DEV-073 assignment/movement boundary for one general commanding one army. */
public final class ArmyCommanderService {
    private final GameState gameState;

    public ArmyCommanderService(GameState gameState) {
        this.gameState = Objects.requireNonNull(gameState, "gameState");
    }

    public void assign(String generalId, String armyId) {
        GeneralState general = requireGeneral(generalId);
        ArmyState army = requireArmy(armyId);
        requireSameNation(general, army);
        if (army.status() != ArmyStatus.STATIONED)
            throw new IllegalStateException("army must be STATIONED to change commander");
        if (gameState.commandedArmyId(generalId).isPresent())
            throw new IllegalStateException("general is already assigned to an army");
        if (army.commanderGeneralId().isPresent())
            throw new IllegalStateException("army already has a commander");
        army.setCommanderGeneralId(generalId);
    }

    public void move(String generalId, String destinationArmyId) {
        GeneralState general = requireGeneral(generalId);
        String sourceArmyId = gameState.commandedArmyId(generalId)
                .orElseThrow(() -> new IllegalStateException("general is not assigned to an army"));
        ArmyState source = requireArmy(sourceArmyId);
        ArmyState destination = requireArmy(destinationArmyId);
        requireSameNation(general, destination);
        if (source.armyId().equals(destination.armyId())) return;
        if (source.status() != ArmyStatus.STATIONED || destination.status() != ArmyStatus.STATIONED)
            throw new IllegalStateException("both armies must be STATIONED to move commander");
        if (!source.locationPointId().equals(destination.locationPointId()))
            throw new IllegalStateException("armies must be at the same point to move commander");
        if (destination.commanderGeneralId().isPresent())
            throw new IllegalStateException("destination army already has a commander");

        source.clearCommanderGeneralId();
        destination.setCommanderGeneralId(generalId);
    }

    public void unassign(String generalId) {
        String armyId = gameState.commandedArmyId(generalId)
                .orElseThrow(() -> new IllegalStateException("general is not assigned to an army"));
        ArmyState army = requireArmy(armyId);
        if (army.status() != ArmyStatus.STATIONED)
            throw new IllegalStateException("army must be STATIONED to remove commander");
        army.clearCommanderGeneralId();
    }

    private GeneralState requireGeneral(String id) {
        return gameState.general(id).orElseThrow(() -> new IllegalArgumentException("general not found: " + id));
    }
    private ArmyState requireArmy(String id) {
        return gameState.army(id).orElseThrow(() -> new IllegalArgumentException("army not found: " + id));
    }
    private static void requireSameNation(GeneralState general, ArmyState army) {
        if (!general.ownerNationId().equals(army.ownerNationId()))
            throw new IllegalArgumentException("general and army must belong to the same nation");
    }
}
