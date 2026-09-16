package kr.danta.paper.combat.live;

/** DEV-115 target eligibility limited to tracked opposing live-combat units. */
public final class CombatTargetPolicy {
    public boolean mayTarget(LiveCombatUnit attacker, LiveCombatUnit candidate) {
        if (attacker == null || candidate == null) return false;
        if (attacker.unitId().equals(candidate.unitId())) return false;
        return attacker.side() != candidate.side();
    }
}
