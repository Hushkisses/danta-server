package kr.danta.paper.combat.live;

/** DEV-115 bridge from legacy step-speed fixtures to Paper native mob navigation. */
public final class CombatMobNavigationPolicy {
    private CombatMobNavigationPolicy() {}

    public static double pathfinderSpeedMultiplier(double stepSpeed) {
        return Math.max(0.80, Math.min(1.60, stepSpeed * 4.0));
    }

    public static boolean shouldSuppressVanillaTargeting(boolean demoOwned) {
        return demoOwned;
    }
}
