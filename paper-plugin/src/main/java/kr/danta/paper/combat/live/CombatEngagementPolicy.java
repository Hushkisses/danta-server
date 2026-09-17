package kr.danta.paper.combat.live;

import kr.danta.core.combat.ai.CombatAiAction;

/** Small Paper-independent rule for closing melee distance after authored route movement. */
public final class CombatEngagementPolicy {
    private CombatEngagementPolicy() {}

    public static boolean shouldChase(
            CombatAttackPolicy.AttackMode mode,
            double distance,
            double attackRange
    ) {
        return shouldChase(mode, CombatAiAction.ENGAGE, distance, attackRange);
    }

    public static boolean shouldChase(
            CombatAttackPolicy.AttackMode mode,
            CombatAiAction action,
            double distance,
            double attackRange
    ) {
        if (mode == null) throw new NullPointerException("mode");
        if (action == null) throw new NullPointerException("action");
        if (!Double.isFinite(distance) || distance < 0.0) throw new IllegalArgumentException("invalid distance");
        if (!Double.isFinite(attackRange) || attackRange < 0.0) throw new IllegalArgumentException("invalid attackRange");
        if (mode != CombatAttackPolicy.AttackMode.MELEE || distance <= attackRange) return false;

        // ADVANCE/ENGAGE are the only actions allowed to replace their authored route
        // with direct target closing. SCREEN/FLANK/PURSUE keep their tactical route;
        // target priority still determines whom they face/attack once in range.
        return action == CombatAiAction.ADVANCE || action == CombatAiAction.ENGAGE;
    }
}
