package kr.danta.paper.combat.live;

import kr.danta.core.combat.ai.CombatAiAction;

public final class CombatMovementPlanner {
    public CombatMovementIntent plan(CombatAiAction action, TacticalRouteSet routes) {
        if (action == null) throw new NullPointerException("action");
        if (routes == null) throw new NullPointerException("routes");

        TacticalRoute route = switch (action) {
            case HOLD -> routes.hold();
            case ADVANCE, ENGAGE -> routes.advance();
            case RETREAT -> routes.retreat();
            case SCREEN -> routes.screen();
            case FLANK -> routes.leftFlank();
            case PURSUE -> routes.pursuit();
            case SUPPORT -> routes.support();
        };
        return new CombatMovementIntent(action, route);
    }

    public record TacticalRouteSet(
            TacticalRoute hold,
            TacticalRoute advance,
            TacticalRoute retreat,
            TacticalRoute screen,
            TacticalRoute leftFlank,
            TacticalRoute rightFlank,
            TacticalRoute pursuit,
            TacticalRoute support
    ) {
        public TacticalRouteSet {
            if (hold == null || advance == null || retreat == null || screen == null
                    || leftFlank == null || rightFlank == null || pursuit == null || support == null) {
                throw new NullPointerException("routes");
            }
        }
    }
}
