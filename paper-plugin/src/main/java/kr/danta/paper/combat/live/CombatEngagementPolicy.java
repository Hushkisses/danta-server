package kr.danta.paper.combat.live;

import kr.danta.core.combat.TroopType;
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
        validate(mode, action, distance, attackRange);
        if (mode != CombatAttackPolicy.AttackMode.MELEE || distance <= attackRange) return false;

        // General melee units only replace authored movement while directly closing.
        return action == CombatAiAction.ADVANCE || action == CombatAiAction.ENGAGE;
    }

    public static boolean shouldChasePriorityMatchup(
            CombatAttackPolicy.AttackMode mode,
            TroopType attackerType,
            TroopType targetType,
            CombatAiAction action,
            double distance,
            double attackRange
    ) {
        if (attackerType == null) throw new NullPointerException("attackerType");
        if (targetType == null) throw new NullPointerException("targetType");
        validate(mode, action, distance, attackRange);
        if (mode != CombatAttackPolicy.AttackMode.MELEE || distance <= attackRange) return false;

        boolean cavalrySpearPair = (attackerType == TroopType.CAVALRY && targetType == TroopType.SPEARMEN)
                || (attackerType == TroopType.SPEARMEN && targetType == TroopType.CAVALRY);
        if (!cavalrySpearPair) return false;

        // Their flank-screen duel is allowed to override those tactical routes so the two
        // counter-units actually form a side front instead of passing each other at range.
        return action != CombatAiAction.HOLD
                && action != CombatAiAction.RETREAT
                && action != CombatAiAction.SUPPORT;
    }

    private static void validate(
            CombatAttackPolicy.AttackMode mode,
            CombatAiAction action,
            double distance,
            double attackRange
    ) {
        if (mode == null) throw new NullPointerException("mode");
        if (action == null) throw new NullPointerException("action");
        if (!Double.isFinite(distance) || distance < 0.0) throw new IllegalArgumentException("invalid distance");
        if (!Double.isFinite(attackRange) || attackRange < 0.0) throw new IllegalArgumentException("invalid attackRange");
    }
}
