package kr.danta.paper.combat.live;

import kr.danta.core.combat.ai.CombatAiAction;

import java.util.UUID;

/**
 * DEV-115 fallback movement rule.
 *
 * <p>HOLD means stay where the unit currently is, not return to a static demo waypoint.
 * If no target is currently selectable, ordinary combat movement also pauses in place so
 * units do not walk back toward authored formation routes while enemies remain elsewhere.
 * Explicit RETREAT keeps using its tactical route.</p>
 */
public final class CombatMovementFallbackPolicy {
    private CombatMovementFallbackPolicy() {}

    public static boolean shouldHoldCurrentPosition(CombatAiAction action, UUID selectedHostileUnitId) {
        if (action == null) throw new NullPointerException("action");
        if (action == CombatAiAction.HOLD) return true;
        return selectedHostileUnitId == null && action != CombatAiAction.RETREAT;
    }
}
