package kr.danta.core.combat.ai;

import kr.danta.core.combat.TroopType;

import java.util.Objects;

/**
 * DEV-114 deterministic tactical decision layer.
 *
 * Distance thresholds are implementation fixtures for live AI behavior and are not final balance values.
 */
public final class CombatAiController {
    private static final double MELEE_RANGE = 3.0;
    private static final double ARCHER_UNSAFE_RANGE = 4.0;

    public CombatAiDecision decide(CombatAiProfile profile, CombatAiObservation observation) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(observation, "observation");

        if (observation.survivalThreatened()) {
            return decision(CombatAiAction.RETREAT);
        }

        return switch (profile) {
            case INFANTRY -> decideInfantry(observation);
            case SPEARMEN -> decideSpearmen(observation);
            case ARCHERS -> decideArchers(observation);
            case CAVALRY -> decideCavalry(observation);
            case MAGIC -> decideMagic(observation);
        };
    }

    private CombatAiDecision decideInfantry(CombatAiObservation observation) {
        if (observation.nearestHostileDistance() <= MELEE_RANGE) {
            return decision(CombatAiAction.ENGAGE, observation.nearestHostileType());
        }
        return decision(CombatAiAction.ADVANCE);
    }

    private CombatAiDecision decideSpearmen(CombatAiObservation observation) {
        if (observation.nearestHostileType() == TroopType.CAVALRY) {
            return decision(CombatAiAction.SCREEN, TroopType.CAVALRY);
        }
        if (observation.backlineThreatened()) {
            return decision(CombatAiAction.SCREEN);
        }
        if (observation.nearestHostileDistance() <= MELEE_RANGE) {
            return decision(CombatAiAction.ENGAGE, observation.nearestHostileType());
        }
        return decision(CombatAiAction.ADVANCE);
    }

    private CombatAiDecision decideArchers(CombatAiObservation observation) {
        if (observation.backlineThreatened() || observation.nearestHostileDistance() <= ARCHER_UNSAFE_RANGE) {
            return decision(CombatAiAction.RETREAT);
        }
        if (observation.frontlineSupportPresent()) {
            return decision(CombatAiAction.ENGAGE, observation.nearestHostileType());
        }
        return decision(CombatAiAction.HOLD);
    }

    private CombatAiDecision decideCavalry(CombatAiObservation observation) {
        if (observation.spearScreenPresent()) {
            return decision(CombatAiAction.ENGAGE, TroopType.SPEARMEN);
        }
        if (observation.exposedEnemyBackline()) {
            return decision(CombatAiAction.FLANK, observation.nearestHostileType());
        }
        if (observation.hostileRetreating()) {
            return decision(CombatAiAction.PURSUE, observation.nearestHostileType());
        }
        return decision(CombatAiAction.FLANK);
    }

    private CombatAiDecision decideMagic(CombatAiObservation observation) {
        if (observation.backlineThreatened()) {
            return decision(CombatAiAction.RETREAT);
        }
        if (observation.alliedCombatGroupPresent()) {
            return decision(CombatAiAction.SUPPORT);
        }
        return decision(CombatAiAction.HOLD);
    }

    private static CombatAiDecision decision(CombatAiAction action) {
        return new CombatAiDecision(action, null);
    }

    private static CombatAiDecision decision(CombatAiAction action, TroopType preferredTargetType) {
        return new CombatAiDecision(action, preferredTargetType);
    }
}
