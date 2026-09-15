package kr.danta.core.economy;

/**
 * DEV-052 runtime-based economy tick cadence.
 * A tick is due for each crossed 30-minute server-runtime boundary.
 */
public final class EconomyTickService {
    public static final long TICK_INTERVAL_MILLIS = 30L * 60L * 1000L;
    private long lastProcessedBoundary;

    public EconomyTickService(long runtimeElapsedMillis) {
        if (runtimeElapsedMillis < 0L) throw new IllegalArgumentException("runtimeElapsedMillis must be >= 0");
        this.lastProcessedBoundary = runtimeElapsedMillis / TICK_INTERVAL_MILLIS;
    }

    public synchronized int claimDueTicks(long runtimeElapsedMillis) {
        if (runtimeElapsedMillis < 0L) throw new IllegalArgumentException("runtimeElapsedMillis must be >= 0");
        long boundary = runtimeElapsedMillis / TICK_INTERVAL_MILLIS;
        long due = Math.max(0L, boundary - lastProcessedBoundary);
        if (due > Integer.MAX_VALUE) throw new IllegalStateException("too many economy ticks due");
        lastProcessedBoundary = Math.max(lastProcessedBoundary, boundary);
        return (int) due;
    }

    public synchronized long nextTickRuntimeMillis() {
        return Math.multiplyExact(lastProcessedBoundary + 1L, TICK_INTERVAL_MILLIS);
    }
}
