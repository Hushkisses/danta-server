package kr.danta.core.combat;

/**
 * DEV-060 logical battle phases from design v0.3.
 * Detailed mechanics are introduced incrementally by DEV-061~064.
 */
public enum CombatPhase {
    OPENING,
    FRONTLINE,
    BACKLINE,
    MOBILE,
    MORALE,
    RETREAT_PURSUIT
}
