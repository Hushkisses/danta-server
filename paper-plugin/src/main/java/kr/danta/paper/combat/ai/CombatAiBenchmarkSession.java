package kr.danta.paper.combat.ai;

/** Paper-independent accumulator for one DEV-116 live benchmark run. */
public final class CombatAiBenchmarkSession {
    private boolean active;
    private int targetUnits;
    private int aliveUnits;
    private long startedAtMillis;
    private int samples;
    private double minimumTps;
    private double msptSum;
    private double maximumMspt;

    public boolean start(int targetUnits, long nowMillis) {
        if (active) return false;
        if (targetUnits != 40 && targetUnits != 60 && targetUnits != 80) {
            throw new IllegalArgumentException("supported benchmark sizes: 40, 60, 80");
        }
        active = true;
        this.targetUnits = targetUnits;
        aliveUnits = targetUnits;
        startedAtMillis = nowMillis;
        samples = 0;
        minimumTps = 20.0;
        msptSum = 0.0;
        maximumMspt = 0.0;
        return true;
    }

    public void sample(int aliveUnits, long nowMillis, double tps, double mspt) {
        if (!active) return;
        if (aliveUnits < 0 || aliveUnits > targetUnits) throw new IllegalArgumentException("invalid aliveUnits");
        if (!Double.isFinite(tps) || tps < 0.0) throw new IllegalArgumentException("invalid tps");
        if (!Double.isFinite(mspt) || mspt < 0.0) throw new IllegalArgumentException("invalid mspt");
        this.aliveUnits = aliveUnits;
        minimumTps = samples == 0 ? tps : Math.min(minimumTps, tps);
        msptSum += mspt;
        maximumMspt = Math.max(maximumMspt, mspt);
        samples++;
    }

    public Snapshot snapshot(long nowMillis) {
        long elapsed = active ? Math.max(0L, nowMillis - startedAtMillis) : 0L;
        return new Snapshot(active, targetUnits, aliveUnits, elapsed,
                samples == 0 ? 20.0 : minimumTps,
                samples == 0 ? 0.0 : msptSum / samples,
                samples == 0 ? 0.0 : maximumMspt,
                samples);
    }

    public Snapshot finish(long nowMillis) {
        Snapshot result = snapshot(nowMillis);
        active = false;
        return result;
    }

    public boolean active() {
        return active;
    }

    public record Snapshot(
            boolean active,
            int targetUnits,
            int aliveUnits,
            long elapsedMillis,
            double minimumTps,
            double averageMspt,
            double maximumMspt,
            int samples
    ) {}
}
