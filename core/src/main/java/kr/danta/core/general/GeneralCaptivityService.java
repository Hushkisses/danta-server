package kr.danta.core.general;

import kr.danta.core.state.GameState;

import java.util.Objects;

/**
 * DEV-075 prisoner lifecycle. Monetary transfer/diplomacy acceptance are intentionally
 * kept outside this core state transition until those authoritative systems are wired.
 */
public final class GeneralCaptivityService {
    private final GameState gameState;

    public GeneralCaptivityService(GameState gameState) {
        this.gameState = Objects.requireNonNull(gameState, "gameState");
    }

    public GeneralCaptivityState capture(String generalId, String captorNationId,
                                         long currentRuntimeMillis, long detentionDurationMillis) {
        GeneralState general = requireGeneral(generalId);
        requireNation(captorNationId);
        if (general.ownerNationId().equals(captorNationId))
            throw new IllegalArgumentException("captor nation must differ from owner nation");
        if (general.captivityState().isPresent())
            throw new IllegalStateException("general is already captive");
        if (gameState.commandedArmyId(generalId).isPresent())
            throw new IllegalStateException("general must be detached from army before capture");
        if (currentRuntimeMillis < 0L) throw new IllegalArgumentException("currentRuntimeMillis must be >= 0");
        if (detentionDurationMillis <= 0L) throw new IllegalArgumentException("detentionDurationMillis must be > 0");
        long endsAt = Math.addExact(currentRuntimeMillis, detentionDurationMillis);
        GeneralCaptivityState state = new GeneralCaptivityState(captorNationId, currentRuntimeMillis, endsAt);
        general.setCaptivityState(state);
        return state;
    }

    /** Release after a ransom agreement. Economy transfer is performed by the caller atomically before this transition. */
    public void releaseForRansom(String generalId, String captorNationId) {
        releaseByCaptor(generalId, captorNationId);
    }

    /** Release after a prisoner-exchange agreement. Pairing/atomic multi-general exchange belongs to diplomacy orchestration. */
    public void releaseForExchange(String generalId, String captorNationId) {
        releaseByCaptor(generalId, captorNationId);
    }

    public boolean releaseIfDetentionExpired(String generalId, long currentRuntimeMillis) {
        if (currentRuntimeMillis < 0L) throw new IllegalArgumentException("currentRuntimeMillis must be >= 0");
        GeneralState general = requireGeneral(generalId);
        GeneralCaptivityState state = general.captivityState().orElse(null);
        if (state == null || !state.detentionExpired(currentRuntimeMillis)) return false;
        general.clearCaptivityState();
        return true;
    }

    private void releaseByCaptor(String generalId, String captorNationId) {
        GeneralState general = requireGeneral(generalId);
        GeneralCaptivityState state = general.captivityState()
                .orElseThrow(() -> new IllegalStateException("general is not captive"));
        if (!state.captorNationId().equals(captorNationId))
            throw new IllegalArgumentException("only the captor nation may authorize this release");
        general.clearCaptivityState();
    }

    private GeneralState requireGeneral(String id) {
        return gameState.general(id).orElseThrow(() -> new IllegalArgumentException("general not found: " + id));
    }

    private void requireNation(String id) {
        if (!gameState.hasNation(id)) throw new IllegalArgumentException("nation not found: " + id);
    }
}
