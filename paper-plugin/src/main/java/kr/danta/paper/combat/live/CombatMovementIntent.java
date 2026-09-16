package kr.danta.paper.combat.live;

import kr.danta.core.combat.ai.CombatAiAction;

public record CombatMovementIntent(CombatAiAction action, TacticalRoute route) {
    public CombatMovementIntent {
        if (action == null) throw new NullPointerException("action");
        if (route == null) throw new NullPointerException("route");
    }
}
