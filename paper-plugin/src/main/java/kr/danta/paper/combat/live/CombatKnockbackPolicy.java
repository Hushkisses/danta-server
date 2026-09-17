package kr.danta.paper.combat.live;

import kr.danta.core.combat.TroopType;

import java.util.Objects;

/**
 * DEV-115 knockback policy boundary.
 *
 * <p>Current development combat disables knockback for tracked army-vs-army hits so authored
 * formations are not displaced by vanilla combat physics. Future troop grades, charge states,
 * equipment or skills can return a non-zero multiplier here without changing the event boundary.</p>
 */
public final class CombatKnockbackPolicy {

    public static CombatKnockbackPolicy developmentDefaults() {
        return new CombatKnockbackPolicy();
    }

    public boolean shouldSuppress(boolean attackerTrackedCombatUnit, boolean victimTrackedCombatUnit) {
        return attackerTrackedCombatUnit && victimTrackedCombatUnit;
    }

    public double multiplier(TroopType attackerType, TroopType victimType) {
        Objects.requireNonNull(attackerType, "attackerType");
        Objects.requireNonNull(victimType, "victimType");
        return 0.0;
    }
}
