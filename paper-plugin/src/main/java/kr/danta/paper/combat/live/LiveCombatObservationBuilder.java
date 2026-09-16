package kr.danta.paper.combat.live;

import kr.danta.paper.combat.ai.PaperCombatAiObservationFactory;

/** DEV-115 maps live battlefield facts into the DEV-114 tactical snapshot model. */
public final class LiveCombatObservationBuilder {

    public PaperCombatAiObservationFactory.TacticalSnapshot build(LiveCombatController.BattlefieldView view) {
        if (view == null) throw new NullPointerException("view");
        return new PaperCombatAiObservationFactory.TacticalSnapshot(
                view.nearestHostileDistance(),
                view.nearestHostileType(),
                view.frontlineSupportPresent(),
                view.backlineThreatened(),
                view.exposedEnemyBackline(),
                view.hostileRetreating(),
                view.spearScreenPresent(),
                view.survivalThreatened(),
                view.alliedCombatGroupPresent()
        );
    }
}
