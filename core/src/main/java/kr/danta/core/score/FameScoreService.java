package kr.danta.core.score;

import kr.danta.core.state.GameState;

import java.util.Objects;

/**
 * DEV-102 accumulated fame score.
 *
 * <p>This service intentionally owns only the monotonic accumulated-score primitive.
 * Exact point values and event-to-score rules belong to DEV-104 and must not be guessed here.</p>
 */
public final class FameScoreService {
    private final GameState gameState;

    public FameScoreService(GameState gameState) {
        this.gameState = Objects.requireNonNull(gameState, "gameState");
    }

    public long score(String nationId) {
        requireNation(nationId);
        return gameState.fameScore(nationId);
    }

    public long award(String nationId, long amount) {
        requireNation(nationId);
        if (amount <= 0L) throw new IllegalArgumentException("fame award must be > 0");
        return gameState.addFameScore(nationId, amount);
    }

    /** Snapshot restore only. Normal gameplay must use award() so accumulated fame never decreases. */
    public void restore(String nationId, long score) {
        requireNation(nationId);
        if (score < 0L) throw new IllegalArgumentException("fame score must be >= 0");
        gameState.restoreFameScore(nationId, score);
    }

    private void requireNation(String nationId) {
        if (nationId == null || nationId.isBlank() || !gameState.hasNation(nationId)) {
            throw new IllegalArgumentException("nation not found: " + nationId);
        }
    }
}
