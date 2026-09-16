package kr.danta.paper.combat.live;

import kr.danta.core.combat.ai.CombatAiDecision;

import java.util.UUID;

/** DEV-115 execution intent produced from a DEV-114 decision. */
public record LiveCombatExecution(
        CombatAiDecision decision,
        CombatMovementIntent movementIntent,
        UUID selectedHostileUnitId
) {
    public LiveCombatExecution {
        if (decision == null) throw new NullPointerException("decision");
        if (movementIntent == null) throw new NullPointerException("movementIntent");
    }
}
