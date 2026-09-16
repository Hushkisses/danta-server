package kr.danta.core.siege;

/**
 * DEV-113 engagement cadence by strategic-point importance.
 */
public enum SiegeEngagementProfile {
    /** Ordinary point: resolve quickly without a full staged battle. */
    NORMAL_QUICK,
    /** Important point: one decisive battle. */
    MAJOR_SINGLE_BATTLE,
    /** Capital: outer battle -> gate -> plaza battle -> inner gate -> core battle. */
    CAPITAL_THREE_BATTLE
}
