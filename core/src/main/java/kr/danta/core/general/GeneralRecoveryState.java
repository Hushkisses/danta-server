package kr.danta.core.general;

/**
 * DEV-074 runtime-based recovery state.
 * The caller supplies recovery duration because design v0.3 does not fix exact durations.
 */
public record GeneralRecoveryState(
        GeneralHealthStatus status,
        long injuredAtRuntimeMillis,
        long recoveryReadyAtRuntimeMillis
) {
    public GeneralRecoveryState {
        if (status == null || status == GeneralHealthStatus.HEALTHY)
            throw new IllegalArgumentException("recovery state requires an injured status");
        if (injuredAtRuntimeMillis < 0L)
            throw new IllegalArgumentException("injuredAtRuntimeMillis must be >= 0");
        if (recoveryReadyAtRuntimeMillis <= injuredAtRuntimeMillis)
            throw new IllegalArgumentException("recoveryReadyAtRuntimeMillis must be after injury time");
    }

    public boolean isRecoveryReady(long runtimeMillis) {
        return runtimeMillis >= recoveryReadyAtRuntimeMillis;
    }
}
