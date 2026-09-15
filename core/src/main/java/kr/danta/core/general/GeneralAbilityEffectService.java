package kr.danta.core.general;

import kr.danta.core.state.GameState;

import java.util.Objects;

/**
 * Resolves active general ability hooks from authoritative assignment state.
 * It does not own economy/combat logic and therefore avoids duplicating existing services.
 */
public final class GeneralAbilityEffectService {
    private final GameState gameState;
    private final GeneralAbilityBalance balance;

    public GeneralAbilityEffectService(GameState gameState, GeneralAbilityBalance balance) {
        this.gameState = Objects.requireNonNull(gameState);
        this.balance = Objects.requireNonNull(balance);
    }

    public double pointMultiplier(String pointId, GeneralAbilityHook hook) {
        return gameState.strategicPoint(pointId)
                .flatMap(point -> point.assignedGeneralId())
                .flatMap(gameState::general)
                .filter(general -> hasHook(general, hook))
                .map(ignored -> balance.multiplier(hook))
                .orElse(1.0);
    }

    public double nationAdditiveBonus(String nationId, GeneralAbilityHook hook) {
        boolean active = gameState.strategicPoints().stream()
                .filter(point -> point.ownerNationId().orElse("").equals(nationId))
                .map(point -> point.assignedGeneralId().flatMap(gameState::general).orElse(null))
                .filter(Objects::nonNull)
                .anyMatch(general -> hasHook(general, hook));
        return active ? balance.additiveBonus(hook) : 0.0;
    }

    public double armyMultiplier(String armyId, GeneralAbilityHook hook) {
        return gameState.army(armyId)
                .flatMap(army -> army.commanderGeneralId())
                .flatMap(gameState::general)
                .filter(general -> hasHook(general, hook))
                .map(ignored -> balance.multiplier(hook))
                .orElse(1.0);
    }

    private static boolean hasHook(GeneralState general, GeneralAbilityHook hook) {
        return general.abilities().stream()
                .map(GeneralAbility::abilityId)
                .map(GeneralAbilityCatalog::find)
                .flatMap(java.util.Optional::stream)
                .anyMatch(effect -> effect.hook() == hook);
    }
}
