package kr.danta.core.combat;

import java.util.Objects;

/**
 * DEV-063 battlefield-capacity calculation.
 *
 * Troops above capacity are represented as reserves. The design says terrain and
 * battlefield width limit simultaneous force use, but does not yet fix a final
 * over-capacity combat-power formula, so no arbitrary penalty is applied here.
 */
public final class BattlefieldCapacityPolicy {
    public BattlefieldSideResult evaluate(CombatSideInput side, BattlefieldContext battlefield) {
        Objects.requireNonNull(side, "side");
        Objects.requireNonNull(battlefield, "battlefield");
        int committed = Math.min(side.troopCount(), battlefield.capacity());
        int reserve = side.troopCount() - committed;
        double utilization = committed / (double) side.troopCount();
        return new BattlefieldSideResult(side.sideId(), side.troopCount(), battlefield.capacity(),
                committed, reserve, utilization);
    }
}
