package kr.danta.paper.combat.live;

/** Small Paper-independent rule for closing melee distance after authored route movement. */
public final class CombatEngagementPolicy {
    private CombatEngagementPolicy() {}

    public static boolean shouldChase(
            CombatAttackPolicy.AttackMode mode,
            double distance,
            double attackRange
    ) {
        if (mode == null) throw new NullPointerException("mode");
        if (!Double.isFinite(distance) || distance < 0.0) throw new IllegalArgumentException("invalid distance");
        if (!Double.isFinite(attackRange) || attackRange < 0.0) throw new IllegalArgumentException("invalid attackRange");
        return mode == CombatAttackPolicy.AttackMode.MELEE && distance > attackRange;
    }
}
