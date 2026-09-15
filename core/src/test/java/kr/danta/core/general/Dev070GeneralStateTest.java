package kr.danta.core.general;

import kr.danta.core.nation.NationState;
import kr.danta.core.state.GameState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Dev070GeneralStateTest {
    @Test void gradeLadderIsFThroughS() {
        assertArrayEquals(new GeneralGrade[]{
                GeneralGrade.F, GeneralGrade.E, GeneralGrade.D, GeneralGrade.C,
                GeneralGrade.B, GeneralGrade.A, GeneralGrade.S
        }, GeneralGrade.values());
    }

    @Test void levelRangeIsOneThroughTen() {
        assertThrows(IllegalArgumentException.class, () -> new GeneralState("g1", "red", GeneralGrade.F, 0));
        assertDoesNotThrow(() -> new GeneralState("g1", "red", GeneralGrade.F, 1));
        assertDoesNotThrow(() -> new GeneralState("g10", "red", GeneralGrade.S, 10));
        assertThrows(IllegalArgumentException.class, () -> new GeneralState("g11", "red", GeneralGrade.S, 11));
    }

    @Test void levelUpStopsAtTen() {
        GeneralState general = new GeneralState("g", "red", GeneralGrade.C, 9);
        general.levelUp();
        assertEquals(10, general.level());
        assertFalse(general.canLevelUp());
        assertThrows(IllegalStateException.class, general::levelUp);
    }

    @Test void serviceRequiresExistingNationAndPreventsDuplicateId() {
        GameState state = new GameState();
        state.addNation(new NationState("red", "Red", "capital_red"));
        GeneralService service = new GeneralService(state);

        GeneralState general = service.create("general_1", "red", GeneralGrade.B, 3);
        assertSame(general, state.general("general_1").orElseThrow());
        assertThrows(IllegalArgumentException.class,
                () -> service.create("general_1", "red", GeneralGrade.A, 4));
        assertThrows(IllegalArgumentException.class,
                () -> service.create("general_2", "missing", GeneralGrade.A, 4));
    }
}
