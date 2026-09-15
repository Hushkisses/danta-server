package kr.danta.core.combat;

import kr.danta.core.state.GameState;
import kr.danta.core.territory.TerritoryService;

import java.util.Objects;

/**
 * DEV-044 minimum combat-to-occupation transition.
 * Occupation is permitted only after a non-draw attacker victory and after the defending
 * point garrison has been reduced to zero by the caller's combat resolution.
 */
public final class CombatOccupationService {
    private final GameState gameState;
    private final TerritoryService territoryService;

    public CombatOccupationService(GameState gameState, TerritoryService territoryService) {
        this.gameState = Objects.requireNonNull(gameState, "gameState");
        this.territoryService = Objects.requireNonNull(territoryService, "territoryService");
    }

    public OccupationResult occupyAfterVictory(String pointId, String attackerNationId,
                                                CombatResolution resolution, boolean attackerIsFirst) {
        Objects.requireNonNull(resolution, "resolution");
        if (!gameState.hasNation(attackerNationId))
            throw new IllegalArgumentException("attacker nation does not exist: " + attackerNationId);
        GarrisonState garrison = gameState.garrison(pointId)
                .orElseThrow(() -> new IllegalArgumentException("garrison does not exist: " + pointId));

        CombatLossResult attacker = attackerIsFirst ? resolution.first() : resolution.second();
        CombatLossResult defender = attackerIsFirst ? resolution.second() : resolution.first();
        if (attacker.outcome() != CombatOutcome.VICTORY || defender.outcome() != CombatOutcome.DEFEAT)
            return new OccupationResult(pointId, attackerNationId, false, "ATTACKER_DID_NOT_WIN");
        if (defender.remainingTroops() > 0)
            return new OccupationResult(pointId, attackerNationId, false, "DEFENDER_REMAINS");

        garrison.setTroopCount(0);
        gameState.removeGarrison(pointId);
        TerritoryService.OwnershipChangeResult ownership =
                territoryService.changeOwner(pointId, attackerNationId, "combat-occupation");
        return new OccupationResult(pointId, attackerNationId, ownership.changed(), "OCCUPIED");
    }

    public record OccupationResult(String pointId, String attackerNationId, boolean ownershipChanged, String reason) {}
}
