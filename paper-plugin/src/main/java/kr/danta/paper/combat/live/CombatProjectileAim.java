package kr.danta.paper.combat.live;

/** Paper-independent ballistic helper for short-range DEV combat projectiles. */
public final class CombatProjectileAim {
    private CombatProjectileAim() {}

    /**
     * Development calibration used by live archers. The lift is expressed as extra vertical aim
     * per horizontal block so the Paper adapter can be tuned independently from troop balance.
     */
    public static AimOffset compensatedOffset(
            double horizontalDistance,
            double verticalDifference,
            double liftPerHorizontalBlock
    ) {
        if (!Double.isFinite(horizontalDistance) || horizontalDistance < 0.0) {
            throw new IllegalArgumentException("invalid horizontalDistance");
        }
        if (!Double.isFinite(verticalDifference)) {
            throw new IllegalArgumentException("invalid verticalDifference");
        }
        if (!Double.isFinite(liftPerHorizontalBlock) || liftPerHorizontalBlock < 0.0) {
            throw new IllegalArgumentException("invalid liftPerHorizontalBlock");
        }
        return new AimOffset(0.0, verticalDifference + horizontalDistance * liftPerHorizontalBlock);
    }

    /** Physics-oriented helper retained for future projectile tuning. */
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
