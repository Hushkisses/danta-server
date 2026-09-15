package kr.danta.core.general;

import kr.danta.core.state.GameState;

import java.util.Objects;

/** DEV-074 applies and completes runtime-based general injuries without inventing durations. */
public final class GeneralRecoveryService {
    private final GameState gameState;

    public GeneralRecoveryService(GameState gameState) {
        this.gameState = Objects.requireNonNull(gameState, "gameState");
    }

    public GeneralRecoveryState injure(String generalId, GeneralHealthStatus status,
                                       long currentRuntimeMillis, long recoveryDurationMillis) {
        GeneralState general = requireGeneral(generalId);
        if (status == null || status == GeneralHealthStatus.HEALTHY)
            throw new IllegalArgumentException("injury status must be INJURED or SEVERELY_INJURED");
        if (currentRuntimeMillis < 0L) throw new IllegalArgumentException("currentRuntimeMillis must be >= 0");
        if (recoveryDurationMillis <= 0L) throw new IllegalArgumentException("recoveryDurationMillis must be > 0");
        long readyAt = Math.addExact(currentRuntimeMillis, recoveryDurationMillis);
        GeneralRecoveryState recovery = new GeneralRecoveryState(status, currentRuntimeMillis, readyAt);
        general.setRecoveryState(recovery);
        return recovery;
    }

    public boolean recoverIfReady(String generalId, long currentRuntimeMillis) {
        if (currentRuntimeMillis < 0L) throw new IllegalArgumentException("currentRuntimeMillis must be >= 0");
        GeneralState general = requireGeneral(generalId);
        GeneralRecoveryState recovery = general.recoveryState().orElse(null);
        if (recovery == null || !recovery.isRecoveryReady(currentRuntimeMillis)) return false;
        general.clearRecoveryState();
        return true;
    }

    private GeneralState requireGeneral(String generalId) {
        return gameState.general(generalId)
                .orElseThrow(() -> new IllegalArgumentException("general not found: " + generalId));
    }
}
