package kr.danta.paper.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DantaWorldConfigTest {
    @Test
    void defaultWorldNamesAreStableAndDistinct() {
        var config = DantaWorldConfig.defaults();
        assertEquals("danta_main", config.strategicWorldName());
        assertEquals("danta_wild", config.wildernessWorldName());
        assertNotEquals(config.strategicWorldName(), config.wildernessWorldName());
    }

    @Test
    void gateAndArrivalOffsetsAreStableDevelopmentDefaults() {
        var config = DantaWorldConfig.defaults();
        assertEquals(new DantaWorldConfig.RelativePoint(4, 0, 0), config.strategicGateOffset());
        assertEquals(new DantaWorldConfig.RelativePoint(0, 0, 0), config.wildernessGateOffset());
        assertEquals(new DantaWorldConfig.RelativePoint(4, 0, 0), config.wildernessArrivalOffset());
    }
}
