package kr.danta.simulator;

import kr.danta.core.DantaCore;
import kr.danta.core.combat.CombatResolver;
import kr.danta.core.combat.CombatResult;
import kr.danta.core.combat.CombatSideInput;
import kr.danta.core.combat.TroopType;

/** Minimal DEV-041 smoke executable; DEV-042 adds fixed scenario coverage. */
public final class CombatSimulatorApp {
    private CombatSimulatorApp() {}

    public static void main(String[] args) {
        CombatResult result = new CombatResolver().resolve(
                CombatSideInput.neutral("red", TroopType.INFANTRY, 1000),
                CombatSideInput.neutral("blue", TroopType.SPEARMEN, 1000));
        System.out.println("Danta Combat Simulator DEV - core " + DantaCore.VERSION);
        System.out.printf("red=%.2f blue=%.2f winner=%s%n",
                result.first().effectivePower(), result.second().effectivePower(),
                result.winner().orElse("DRAW"));
    }
}
