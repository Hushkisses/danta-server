package kr.danta.paper.combat.ai;

import kr.danta.core.combat.TroopType;
import kr.danta.core.combat.ai.CombatAiObservation;

/**
 * DEV-114 Paper boundary: converts Paper-facing tactical facts into the
 * Paper-independent core observation model. No Bukkit entity objects leak into core.
 */
public final class PaperCombatAiObservationFactory {

    public CombatAiObservation from(TacticalSnapshot snapshot) {
        if (snapshot == null) throw new NullPointerException("snapshot");
        return new CombatAiObservation(
                snapshot.nearestHostileDistance(),
                snapshot.nearestHostileType(),
                snapshot.frontlineSupportPresent(),
                snapshot.backlineThreatened(),
                snapshot.exposedEnemyBackline(),
                snapshot.hostileRetreating(),
                snapshot.spearScreenPresent(),
                snapshot.survivalThreatened(),
                snapshot.alliedCombatGroupPresent());
    }

    public record TacticalSnapshot(
            double nearestHostileDistance,
            TroopType nearestHostileType,
            boolean frontlineSupportPresent,
            boolean backlineThreatened,
            boolean exposedEnemyBackline,
            boolean hostileRetreating,
            boolean spearScreenPresent,
            boolean survivalThreatened,
            boolean alliedCombatGroupPresent
    ) {}
}
