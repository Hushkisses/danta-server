package kr.danta.paper.combat.live;

/** Paper-independent ballistic helper for short-range DEV combat projectiles. */
public final class CombatProjectileAim {
    private CombatProjectileAim() {}

    /**
     * Returns the vertical offset to aim at after compensating for projectile gravity.
     * The approximation intentionally ignores drag because DEV-115 shots are short ranged.
     */
    public static AimOffset compensatedOffset(
            double horizontalDistance,
            double verticalDifference,
            double projectileSpeed,
            double gravityPerTickSquared
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
        if (!Double.isFinite(gravityPerTickSquared) || gravityPerTickSquared < 0.0) {
            throw new IllegalArgumentException("invalid gravityPerTickSquared");
        }

        double flightTicks = horizontalDistance / projectileSpeed;
        double gravityCompensation = 0.5 * gravityPerTickSquared * flightTicks * flightTicks;
        return new AimOffset(0.0, verticalDifference + gravityCompensation);
    }

    public record AimOffset(double horizontal, double vertical) {}
}
