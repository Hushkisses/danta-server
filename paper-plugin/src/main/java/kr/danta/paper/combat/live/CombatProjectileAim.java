package kr.danta.paper.combat.live;

/** Paper-independent ballistic helper for short-range DEV combat projectiles. */
public final class CombatProjectileAim {
    private static final int SOLVER_ITERATIONS = 48;
    private static final int MAX_SIMULATION_TICKS = 80;

    private CombatProjectileAim() {}

    /**
     * Solves a launch angle against the simplified Minecraft arrow model used by the DEV runtime.
     * The returned vertical value is an equivalent aim offset: atan2(vertical, horizontalDistance)
     * is the launch angle to use before normalizing the shot vector.
     */
    public static AimOffset compensatedOffset(
            double horizontalDistance,
            double verticalDifference,
            double projectileSpeed,
            double gravityPerTick,
            double drag
    ) {
        validate(horizontalDistance, verticalDifference, projectileSpeed, gravityPerTick, drag);
        if (horizontalDistance == 0.0) {
            return new AimOffset(0.0, verticalDifference);
        }

        double low = Math.toRadians(-35.0);
        double high = Math.toRadians(55.0);
        for (int i = 0; i < SOLVER_ITERATIONS; i++) {
            double mid = (low + high) * 0.5;
            double simulated = simulatedHeightAtDistance(
                    horizontalDistance, projectileSpeed, mid, gravityPerTick, drag);
            if (simulated < verticalDifference) {
                low = mid;
            } else {
                high = mid;
            }
        }

        double angle = (low + high) * 0.5;
        return new AimOffset(0.0, Math.tan(angle) * horizontalDistance);
    }

    static double simulatedHeightAtDistance(
            double horizontalDistance,
            double projectileSpeed,
            double angleRadians,
            double gravityPerTick,
            double drag
    ) {
        validate(horizontalDistance, 0.0, projectileSpeed, gravityPerTick, drag);
        if (!Double.isFinite(angleRadians)) {
            throw new IllegalArgumentException("invalid angleRadians");
        }
        if (horizontalDistance == 0.0) return 0.0;

        double x = 0.0;
        double y = 0.0;
        double vx = projectileSpeed * Math.cos(angleRadians);
        double vy = projectileSpeed * Math.sin(angleRadians);

        for (int tick = 0; tick < MAX_SIMULATION_TICKS; tick++) {
            double previousX = x;
            double previousY = y;
            x += vx;
            y += vy;

            if (x >= horizontalDistance && x > previousX) {
                double fraction = (horizontalDistance - previousX) / (x - previousX);
                return previousY + (y - previousY) * fraction;
            }

            vx *= drag;
            vy = vy * drag - gravityPerTick;
            if (vx <= 1.0e-6) break;
        }
        return Double.NEGATIVE_INFINITY;
    }

    private static void validate(
            double horizontalDistance,
            double verticalDifference,
            double projectileSpeed,
            double gravityPerTick,
            double drag
    ) {
        if (!Double.isFinite(horizontalDistance) || horizontalDistance < 0.0) {
            throw new IllegalArgumentException("invalid horizontalDistance");
        }
        if (!Double.isFinite(verticalDifference)) {
            throw new IllegalArgumentException("invalid verticalDifference");
        }
        if (!Double.isFinite(projectileSpeed) || projectileSpeed <= 0.0) {
            throw new IllegalArgumentException("invalid projectileSpeed");
        }
        if (!Double.isFinite(gravityPerTick) || gravityPerTick < 0.0) {
            throw new IllegalArgumentException("invalid gravityPerTick");
        }
        if (!Double.isFinite(drag) || drag <= 0.0 || drag > 1.0) {
            throw new IllegalArgumentException("invalid drag");
        }
    }

    public record AimOffset(double horizontal, double vertical) {}
}
