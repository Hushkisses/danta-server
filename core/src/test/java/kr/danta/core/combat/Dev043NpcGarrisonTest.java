package kr.danta.core.combat;

import kr.danta.core.state.GameState;
import kr.danta.core.territory.PointPosition;
import kr.danta.core.territory.StrategicPoint;
import kr.danta.core.territory.StrategicPointType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Dev043NpcGarrisonTest {
    @Test void createsNpcGarrisonOnExistingPoint() {
        GameState state = state();
        GarrisonState garrison = new GarrisonService(state).createNpcGarrison("market", 500);
        assertEquals(GarrisonController.NPC, garrison.controller());
        assertTrue(garrison.nationId().isEmpty());
        assertEquals(500, garrison.troopCount());
        assertSame(garrison, state.garrison("market").orElseThrow());
    }

    @Test void rejectsMissingPointAndDuplicateGarrison() {
        GameState state = state();
        GarrisonService service = new GarrisonService(state);
        assertThrows(IllegalArgumentException.class, () -> service.createNpcGarrison("missing", 100));
        service.createNpcGarrison("market", 100);
        assertThrows(IllegalArgumentException.class, () -> service.createNpcGarrison("market", 200));
    }

    @Test void nationControllerRequiresNationIdButNpcDoesNot() {
        GarrisonState garrison = GarrisonState.npc("market", 100);
        assertDoesNotThrow(() -> garrison.setController(GarrisonController.NPC, null));
        assertThrows(NullPointerException.class,
                () -> garrison.setController(GarrisonController.NATION, null));
        garrison.setController(GarrisonController.NATION, "red");
        assertEquals("red", garrison.nationId().orElseThrow());
    }

    private static GameState state() {
        GameState state = new GameState();
        state.addStrategicPoint(new StrategicPoint("market", "시장", StrategicPointType.MAJOR,
                new PointPosition("world", 0, 70, 0), 2));
        return state;
    }
}
