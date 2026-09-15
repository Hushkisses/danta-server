package kr.danta.core.combat;

import java.util.List;

/** Fixed deterministic scenarios used as the DEV-042 regression baseline. */
public final class DeterministicCombatScenarios {
    private DeterministicCombatScenarios() {}

    public static List<CombatScenario> all() {
        return List.of(
                scenario("equal-infantry-draw", TroopType.INFANTRY, 1000, TroopType.INFANTRY, 1000, null, 1000, 1000),
                scenario("infantry-beats-spearmen", TroopType.INFANTRY, 1000, TroopType.SPEARMEN, 1000, "red", 1250, 1000),
                scenario("spearmen-beats-cavalry", TroopType.SPEARMEN, 1000, TroopType.CAVALRY, 1000, "red", 1250, 1000),
                scenario("cavalry-beats-archers", TroopType.CAVALRY, 1000, TroopType.ARCHERS, 1000, "red", 1250, 1000),
                scenario("archers-beat-infantry", TroopType.ARCHERS, 1000, TroopType.INFANTRY, 1000, "red", 1250, 1000),
                scenario("numbers-overcome-soft-counter", TroopType.INFANTRY, 1000, TroopType.SPEARMEN, 1300, "blue", 1250, 1300)
        );
    }

    private static CombatScenario scenario(String id, TroopType firstType, int firstCount,
                                           TroopType secondType, int secondCount, String winner,
                                           double firstPower, double secondPower) {
        return new CombatScenario(id,
                CombatSideInput.neutral("red", firstType, firstCount),
                CombatSideInput.neutral("blue", secondType, secondCount),
                winner, firstPower, secondPower);
    }
}
