package kr.danta.paper.world;

/** Pure DEV-MAP-003 travel eligibility policy, independent from Bukkit. */
public final class ExplorerGuildTravelPolicy {
    public boolean canTravel(WorldRole from, TravelGate gate) {
        if (from == null || gate == null) return false;
        return (from == WorldRole.STRATEGIC_MAIN && gate == TravelGate.EXPLORERS_GUILD)
                || (from == WorldRole.WILDERNESS && gate == TravelGate.WILDERNESS_RETURN);
    }

    public WorldRole destinationRole(WorldRole from, TravelGate gate) {
        if (!canTravel(from, gate)) {
            throw new IllegalStateException("travel gate is not valid for current world role");
        }
        return from == WorldRole.STRATEGIC_MAIN ? WorldRole.WILDERNESS : WorldRole.STRATEGIC_MAIN;
    }
}
