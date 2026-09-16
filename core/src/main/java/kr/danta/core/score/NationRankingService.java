package kr.danta.core.score;

import kr.danta.core.nation.NationState;
import kr.danta.core.state.GameState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** DEV-106 ranking projection shared by live ranking UI and final season results. */
public final class NationRankingService {
    private final GameState gameState;
    private final FameScoreService fame;
    private final HegemonyScoreService hegemony;

    public NationRankingService(GameState gameState, FameScoreService fame, HegemonyScoreService hegemony) {
        this.gameState = Objects.requireNonNull(gameState, "gameState");
        this.fame = Objects.requireNonNull(fame, "fame");
        this.hegemony = Objects.requireNonNull(hegemony, "hegemony");
    }

    public List<RankedNation> ranking() {
        List<RankedNation> rows = new ArrayList<>();
        for (NationState nation : gameState.nations()) {
            long fameScore = fame.score(nation.nationId());
            long hegemonyScore = hegemony.score(nation.nationId());
            rows.add(new RankedNation(nation.nationId(), nation.displayName(), fameScore, hegemonyScore,
                    Math.addExact(fameScore, hegemonyScore)));
        }
        rows.sort(Comparator.comparingLong(RankedNation::totalScore).reversed()
                .thenComparing(Comparator.comparingLong(RankedNation::hegemonyScore).reversed())
                .thenComparing(Comparator.comparingLong(RankedNation::fameScore).reversed())
                .thenComparing(RankedNation::nationId));
        return List.copyOf(rows);
    }

    public record RankedNation(String nationId, String displayName, long fameScore, long hegemonyScore, long totalScore) {}
}
