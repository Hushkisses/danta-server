package kr.danta.core.score;

import kr.danta.core.nation.NationState;
import kr.danta.core.nation.NationStatus;
import kr.danta.core.state.GameState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Dev102FameScoreTest {
    @Test
    void accumulatedFameStartsAtZeroAndOnlyIncreases() {
        GameState state = new GameState();
        state.addNation(new NationState("red", "적국", null, 0L, NationStatus.ACTIVE));
        FameScoreService scores = new FameScoreService(state);

        assertEquals(0L, scores.score("red"));
        assertEquals(40L, scores.award("red", 40L));
        assertEquals(65L, scores.award("red", 25L));
        assertEquals(65L, scores.score("red"));
        assertThrows(IllegalArgumentException.class, () -> scores.award("red", 0L));
        assertThrows(IllegalArgumentException.class, () -> scores.award("red", -1L));
    }

    @Test
    void restoreSupportsSnapshotWithoutCreatingUnknownNationScore() {
        GameState state = new GameState();
        state.addNation(new NationState("blue", "청국", null, 0L, NationStatus.ACTIVE));
        FameScoreService scores = new FameScoreService(state);

        scores.restore("blue", 123L);
        assertEquals(123L, scores.score("blue"));
        assertThrows(IllegalArgumentException.class, () -> scores.restore("ghost", 1L));
        assertThrows(IllegalArgumentException.class, () -> scores.restore("blue", -1L));
    }

    @Test
    void overflowIsRejectedInsteadOfWrappingAccumulatedScore() {
        GameState state = new GameState();
        state.addNation(new NationState("red", "적국", null, 0L, NationStatus.ACTIVE));
        FameScoreService scores = new FameScoreService(state);
        scores.restore("red", Long.MAX_VALUE);
        assertThrows(ArithmeticException.class, () -> scores.award("red", 1L));
        assertEquals(Long.MAX_VALUE, scores.score("red"));
    }
}
