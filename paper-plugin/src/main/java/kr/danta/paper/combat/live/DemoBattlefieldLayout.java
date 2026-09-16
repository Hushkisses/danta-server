package kr.danta.paper.combat.live;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** DEV-115 deterministic, Paper-independent demo battlefield geometry. */
public final class DemoBattlefieldLayout {
    private final Map<CombatSide, CombatMovementPlanner.TacticalRouteSet> routesBySide;

    private DemoBattlefieldLayout(Map<CombatSide, CombatMovementPlanner.TacticalRouteSet> routesBySide) {
        this.routesBySide = Map.copyOf(routesBySide);
    }

    public static DemoBattlefieldLayout around(double originX, double originY, double originZ) {
        EnumMap<CombatSide, CombatMovementPlanner.TacticalRouteSet> routes = new EnumMap<>(CombatSide.class);
        routes.put(CombatSide.RED, routesForRed(originX, originY, originZ));
        routes.put(CombatSide.BLUE, routesForBlue(originX, originY, originZ));
        return new DemoBattlefieldLayout(routes);
    }

    public CombatMovementPlanner.TacticalRouteSet routes(CombatSide side) {
        CombatMovementPlanner.TacticalRouteSet routes = routesBySide.get(side);
        if (routes == null) throw new IllegalArgumentException("unknown combat side: " + side);
        return routes;
    }

    private static CombatMovementPlanner.TacticalRouteSet routesForRed(double x, double y, double z) {
        return new CombatMovementPlanner.TacticalRouteSet(
                route("red-hold", x - 12, y, z),
                route("red-advance", x - 6, y, z, x - 1.5, y, z),
                route("red-retreat", x - 14, y, z),
                route("red-screen", x - 5, y, z),
                route("red-left-flank", x - 6, y, z - 8, x + 1.5, y, z - 8),
                route("red-right-flank", x - 6, y, z + 8, x + 1.5, y, z + 8),
                route("red-pursuit", x + 6, y, z),
                route("red-support", x - 14, y, z + 3)
        );
    }

    private static CombatMovementPlanner.TacticalRouteSet routesForBlue(double x, double y, double z) {
        return new CombatMovementPlanner.TacticalRouteSet(
                route("blue-hold", x + 12, y, z),
                route("blue-advance", x + 6, y, z, x + 1.5, y, z),
                route("blue-retreat", x + 14, y, z),
                route("blue-screen", x + 5, y, z),
                route("blue-left-flank", x + 6, y, z + 8, x - 1.5, y, z + 8),
                route("blue-right-flank", x + 6, y, z - 8, x - 1.5, y, z - 8),
                route("blue-pursuit", x - 6, y, z),
                route("blue-support", x + 14, y, z - 3)
        );
    }

    private static TacticalRoute route(String id, double... coordinates) {
        if (coordinates.length == 0 || coordinates.length % 3 != 0) {
            throw new IllegalArgumentException("coordinates must be x/y/z triples");
        }
        java.util.ArrayList<TacticalWaypoint> waypoints = new java.util.ArrayList<>();
        for (int i = 0; i < coordinates.length; i += 3) {
            waypoints.add(new TacticalWaypoint(
                    id + "-" + (waypoints.size() + 1),
                    coordinates[i],
                    coordinates[i + 1],
                    coordinates[i + 2]
            ));
        }
        return new TacticalRoute(id, List.copyOf(waypoints));
    }
}
