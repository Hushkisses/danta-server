package kr.danta.paper.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExplorerGuildTravelPolicyTest {
    @Test
    void mainlandGuildGateTravelsOnlyToWilderness() {
        var policy = new ExplorerGuildTravelPolicy();
        assertTrue(policy.canTravel(WorldRole.STRATEGIC_MAIN, TravelGate.EXPLORERS_GUILD));
        assertEquals(WorldRole.WILDERNESS,
                policy.destinationRole(WorldRole.STRATEGIC_MAIN, TravelGate.EXPLORERS_GUILD));
        assertFalse(policy.canTravel(WorldRole.STRATEGIC_MAIN, TravelGate.WILDERNESS_RETURN));
    }

    @Test
    void wildernessReturnGateTravelsOnlyToMainland() {
        var policy = new ExplorerGuildTravelPolicy();
        assertTrue(policy.canTravel(WorldRole.WILDERNESS, TravelGate.WILDERNESS_RETURN));
        assertEquals(WorldRole.STRATEGIC_MAIN,
                policy.destinationRole(WorldRole.WILDERNESS, TravelGate.WILDERNESS_RETURN));
        assertFalse(policy.canTravel(WorldRole.WILDERNESS, TravelGate.EXPLORERS_GUILD));
    }

    @Test
    void invalidPairCannotResolveDestination() {
        var policy = new ExplorerGuildTravelPolicy();
        assertThrows(IllegalStateException.class,
                () -> policy.destinationRole(WorldRole.WILDERNESS, TravelGate.EXPLORERS_GUILD));
    }
}
