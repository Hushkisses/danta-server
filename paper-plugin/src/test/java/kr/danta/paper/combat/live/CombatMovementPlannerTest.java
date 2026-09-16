package kr.danta.paper.combat.live;

import kr.danta.core.combat.ai.CombatAiAction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CombatMovementPlannerTest {
    private static TacticalRoute route(String id, double x) {
        return new TacticalRoute(id, List.of(new TacticalWaypoint(id + "-1", x, 64, 0)));
    }

    private static CombatMovementPlanner.TacticalRouteSet routes() {
        return new CombatMovementPlanner.TacticalRouteSet(
                route("hold", 0), route("advance", 10), route("retreat", -10),
                route("screen", 4), route("left", 8), route("right", 8),
                route("pursuit", 14), route("support", -4));
    }

    @Test
    void mapsCombatAiActionsToAuthoredRoutes() {
        CombatMovementPlanner planner = new CombatMovementPlanner();
        assertEquals("hold", planner.plan(CombatAiAction.HOLD, routes()).route().id());
        assertEquals("advance", planner.plan(CombatAiAction.ADVANCE, routes()).route().id());
        assertEquals("advance", planner.plan(CombatAiAction.ENGAGE, routes()).route().id());
        assertEquals("retreat", planner.plan(CombatAiAction.RETREAT, routes()).route().id());
        assertEquals("screen", planner.plan(CombatAiAction.SCREEN, routes()).route().id());
        assertTrue(List.of("left", "right").contains(planner.plan(CombatAiAction.FLANK, routes()).route().id()));
        assertEquals("pursuit", planner.plan(CombatAiAction.PURSUE, routes()).route().id());
        assertEquals("support", planner.plan(CombatAiAction.SUPPORT, routes()).route().id());
    }
}
