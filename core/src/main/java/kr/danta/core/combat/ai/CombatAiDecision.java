package kr.danta.core.combat.ai;

import kr.danta.core.combat.TroopType;

import java.util.Objects;

public record CombatAiDecision(
        CombatAiAction action,
        TroopType preferredTargetType
) {
    public CombatAiDecision {
        Objects.requireNonNull(action, "action");
    }
}
