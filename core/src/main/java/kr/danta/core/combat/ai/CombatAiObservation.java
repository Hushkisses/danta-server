package kr.danta.core.combat.ai;

import kr.danta.core.combat.TroopType;

public record CombatAiObservation(
        double nearestHostileDistance,
        TroopType nearestHostileType,
        boolean frontlineSupportPresent,
        boolean backlineThreatened,
        boolean exposedEnemyBackline,
        boolean hostileRetreating,
        boolean spearScreenPresent,
        boolean survivalThreatened,
        boolean alliedCombatGroupPresent
) {
    public CombatAiObservation {
        if (!Double.isFinite(nearestHostileDistance) || nearestHostileDistance < 0.0) {
            throw new IllegalArgumentException("nearestHostileDistance must be finite and non-negative");
        }
    }
}
