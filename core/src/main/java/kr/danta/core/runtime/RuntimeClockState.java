package kr.danta.core.runtime;

public record RuntimeClockState(long elapsedMillis, boolean paused, double speedMultiplier) {
    public RuntimeClockState {
        if (elapsedMillis < 0L) throw new IllegalArgumentException("elapsedMillis must be >= 0");
        if (!Double.isFinite(speedMultiplier) || speedMultiplier <= 0.0d) throw new IllegalArgumentException("invalid speedMultiplier");
    }
}
