package kr.danta.core.runtime;

import java.time.Duration;
import java.util.Objects;
import java.util.function.LongSupplier;

/** Server-runtime clock. Advances only while running. */
public final class RuntimeClockService {
    private final LongSupplier nanoTimeSource;
    private long accumulatedNanos;
    private long startedAtNanos;
    private boolean running;
    private boolean paused;
    private double speedMultiplier = 1.0d;

    public RuntimeClockService() { this(System::nanoTime); }
    RuntimeClockService(LongSupplier source) { this.nanoTimeSource = Objects.requireNonNull(source); }

    public synchronized void start() {
        if (running) return;
        startedAtNanos = nanoTimeSource.getAsLong();
        running = true;
    }

    public synchronized void stop() {
        checkpoint();
        running = false;
    }

    public synchronized void pause() {
        if (paused) return;
        checkpoint();
        paused = true;
    }

    public synchronized void resume() {
        if (!paused) return;
        startedAtNanos = nanoTimeSource.getAsLong();
        paused = false;
    }

    public synchronized void setSpeedMultiplier(double multiplier) {
        if (!Double.isFinite(multiplier) || multiplier <= 0.0d || multiplier > 100.0d) {
            throw new IllegalArgumentException("speedMultiplier must be > 0 and <= 100");
        }
        checkpoint();
        speedMultiplier = multiplier;
    }

    public synchronized void setElapsedMillis(long millis) {
        if (millis < 0L) throw new IllegalArgumentException("millis must be >= 0");
        accumulatedNanos = millis * 1_000_000L;
        startedAtNanos = nanoTimeSource.getAsLong();
    }

    public synchronized boolean isRunning() { return running; }
    public synchronized boolean isPaused() { return paused; }
    public synchronized double speedMultiplier() { return speedMultiplier; }
    public synchronized Duration elapsed() { return Duration.ofNanos(elapsedNanos()); }
    public synchronized long elapsedMillis() { return elapsedNanos() / 1_000_000L; }

    private void checkpoint() {
        if (running && !paused) accumulatedNanos += scaledElapsedSinceStartNanos();
        startedAtNanos = nanoTimeSource.getAsLong();
    }

    private long elapsedNanos() {
        return accumulatedNanos + ((running && !paused) ? scaledElapsedSinceStartNanos() : 0L);
    }

    private long scaledElapsedSinceStartNanos() {
        long delta = Math.max(0L, nanoTimeSource.getAsLong() - startedAtNanos);
        return Math.max(0L, Math.round(delta * speedMultiplier));
    }
}
