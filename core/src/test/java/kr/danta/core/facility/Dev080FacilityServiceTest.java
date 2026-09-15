package kr.danta.core.facility;

import kr.danta.core.state.GameState;
import kr.danta.core.territory.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Dev080FacilityServiceTest {
    @Test void facilityUsesOneSlotAndUpgradesIToIIIWithoutExtraSlots() {
        GameState state = new GameState();
        state.addStrategicPoint(new StrategicPoint("p1","P1", StrategicPointType.FARM,new PointPosition("world",0,64,0),1));
        FacilityService service = new FacilityService(state);
        assertEquals(FacilityTier.I, service.buildTierOne("p1","farm").tier());
        assertEquals(0, service.availableSlots("p1"));
        assertEquals(FacilityTier.II, service.upgrade("p1","farm").tier());
        assertEquals(FacilityTier.III, service.upgrade("p1","farm").tier());
        assertEquals(1, service.facilities("p1").size());
        assertThrows(IllegalStateException.class, () -> service.upgrade("p1","farm"));
    }

    @Test void cannotBuildBeyondStrategicPointSlotCount() {
        GameState state = new GameState();
        state.addStrategicPoint(new StrategicPoint("p1","P1", StrategicPointType.FARM,new PointPosition("world",0,64,0),1));
        FacilityService service = new FacilityService(state);
        service.buildTierOne("p1","farm");
        assertThrows(IllegalStateException.class, () -> service.buildTierOne("p1","warehouse"));
    }
}
