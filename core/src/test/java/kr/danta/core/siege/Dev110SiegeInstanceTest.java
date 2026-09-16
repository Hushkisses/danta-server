package kr.danta.core.siege;

import kr.danta.core.nation.NationState;
import kr.danta.core.state.GameState;
import kr.danta.core.territory.*;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class Dev110SiegeInstanceTest {
    @Test void followsCreatedScheduledActiveResolvedLifecycle() {
        SiegeInstance s = service().create("s1", "fort", "red");
        assertEquals(SiegePhase.CREATED, s.phase());
        s.schedule(); assertEquals(SiegePhase.SCHEDULED, s.phase());
        s.activate(); assertEquals(SiegePhase.ACTIVE, s.phase());
        s.resolve("ATTACKER"); assertEquals(SiegePhase.RESOLVED, s.phase());
        assertEquals("ATTACKER", s.result());
        assertThrows(IllegalStateException.class, s::activate);
    }

    @Test void preventsConcurrentSiegesForSamePoint() {
        SiegeService service = service();
        service.create("s1", "fort", "red");
        assertThrows(IllegalStateException.class, () -> service.create("s2", "fort", "red"));
    }

    @Test void scheduledSiegeMayBeCancelledButActiveOneMayNot() {
        SiegeInstance s = service().create("s1", "fort", "red");
        s.schedule(); s.cancel();
        assertEquals(SiegePhase.CANCELLED, s.phase());
        SiegeInstance next = service().create("s2", "fort", "red");
        next.schedule(); next.activate();
        assertThrows(IllegalStateException.class, next::cancel);
    }

    private static SiegeService service() {
        GameState state = new GameState();
        state.addNation(new NationState("red", "적국"));
        state.addNation(new NationState("blue", "청국"));
        state.addStrategicPoint(new StrategicPoint("fort", "대요새", StrategicPointType.MAJOR, "blue",
                new PointPosition("world", 0, 64, 0), 1, Map.of()));
        return new SiegeService(state);
    }
}
