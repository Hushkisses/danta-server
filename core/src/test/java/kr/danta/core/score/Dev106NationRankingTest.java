package kr.danta.core.score;

import kr.danta.core.nation.NationState;
import kr.danta.core.state.GameState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Dev106NationRankingTest {
    @Test
    void ranksByCombinedFameAndCurrentHegemony() {
        GameState state = new GameState();
        state.addNation(new NationState("red", "적국"));
        state.addNation(new NationState("blue", "청국"));
        FameScoreService fame = new FameScoreService(state);
        fame.award("red", 10);
        fame.award("blue", 20);
        HegemonyScoreService hegemony = new HegemonyScoreService(state, ignored -> 0L);

        var ranking = new NationRankingService(state, fame, hegemony).ranking();

        assertEquals("blue", ranking.get(0).nationId());
        assertEquals(20L, ranking.get(0).totalScore());
        assertEquals("red", ranking.get(1).nationId());
    }

    @Test
    void deterministicTieBreakKeepsFinalResultStable() {
        GameState state = new GameState();
        state.addNation(new NationState("red", "적국"));
        state.addNation(new NationState("blue", "청국"));
        FameScoreService fame = new FameScoreService(state);
        HegemonyScoreService hegemony = new HegemonyScoreService(state, ignored -> 0L);

        var ranking = new NationRankingService(state, fame, hegemony).ranking();

        assertEquals("blue", ranking.get(0).nationId());
        assertEquals("red", ranking.get(1).nationId());
    }
}
