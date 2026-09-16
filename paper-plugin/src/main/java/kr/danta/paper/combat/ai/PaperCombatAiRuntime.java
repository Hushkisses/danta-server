package kr.danta.paper.combat.ai;

import kr.danta.core.combat.TroopType;
import kr.danta.core.combat.ai.CombatAiController;
import kr.danta.core.combat.ai.CombatAiDecision;
import kr.danta.core.combat.ai.CombatAiProfile;

/** DEV-114 Paper-side runtime boundary for deterministic core CombatAI decisions. */
public final class PaperCombatAiRuntime {
    private final CombatAiController controller = new CombatAiController();
    private final PaperCombatAiObservationFactory observationFactory = new PaperCombatAiObservationFactory();

    public CombatAiProfile profileFor(TroopType troopType) {
        if (troopType == null) throw new NullPointerException("troopType");
        return switch (troopType) {
            case INFANTRY -> CombatAiProfile.INFANTRY;
            case SPEARMEN -> CombatAiProfile.SPEARMEN;
            case ARCHERS -> CombatAiProfile.ARCHERS;
            case CAVALRY -> CombatAiProfile.CAVALRY;
            case MAGIC -> CombatAiProfile.MAGIC;
        };
    }

    public CombatAiDecision decide(
            TroopType troopType,
            PaperCombatAiObservationFactory.TacticalSnapshot snapshot
    ) {
        return controller.decide(profileFor(troopType), observationFactory.from(snapshot));
    }
}
