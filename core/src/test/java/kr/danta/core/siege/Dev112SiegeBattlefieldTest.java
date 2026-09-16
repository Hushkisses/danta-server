package kr.danta.core.siege;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class Dev112SiegeBattlefieldTest {
    @Test void definesLargeFortressBoundaryAndObjectiveAnchors() {
        SiegeBattlefield field = new SiegeBattlefield("canyon_fort_test", "canyon_fort", "world",
                790, 78, -120, 64, List.of(
                new SiegeObjective("outer_gate", SiegeObjectiveType.OUTER_GATE, 758, 78, -120),
                new SiegeObjective("inner_gate", SiegeObjectiveType.INNER_GATE, 778, 78, -120),
                new SiegeObjective("core", SiegeObjectiveType.CORE, 790, 80, -120)));
        assertTrue(field.contains("world", 790, -120));
        assertTrue(field.contains("world", 854, -120));
        assertFalse(field.contains("world", 855, -120));
        assertFalse(field.contains("world_nether", 790, -120));
        assertEquals(3, field.objectives().size());
    }
}
