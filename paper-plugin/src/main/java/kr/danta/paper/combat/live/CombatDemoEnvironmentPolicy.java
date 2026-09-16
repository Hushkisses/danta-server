package kr.danta.paper.combat.live;

/** Environment exceptions for temporary DEV-115 demo entities. */
public final class CombatDemoEnvironmentPolicy {
    private CombatDemoEnvironmentPolicy() {}

    public static boolean preventCombustion(boolean demoOwned) {
        return demoOwned;
    }
}
