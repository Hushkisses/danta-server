package kr.danta.paper.combat.live;

import kr.danta.core.combat.TroopType;
import kr.danta.core.combat.ai.CombatAiAction;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LiveCombatControllerTest {

    @Test
    void exposesDistinctDev114ActionsThroughLiveExecutionIntent() {
        LiveCombatController controller = new LiveCombatController();
        DemoBattlefieldLayout layout = DemoBattlefieldLayout.around(100, 64, 100);

        LiveCombatUnit redSpearman = unit(CombatSide.RED, TroopType.SPEARMEN);
        LiveCombatUnit redArcher = unit(CombatSide.RED, TroopType.ARCHERS);
        LiveCombatUnit redCavalry = unit(CombatSide.RED, TroopType.CAVALRY);
        LiveCombatUnit redMagic = unit(CombatSide.RED, TroopType.MAGIC);

        assertEquals(
                CombatAiAction.SCREEN,
                controller.decide(redSpearman, LiveCombatController.BattlefieldView.cavalryThreat(), layout).decision().action());
        assertEquals(
                CombatAiAction.RETREAT,
                controller.decide(redArcher, LiveCombatController.BattlefieldView.closeThreat(), layout).decision().action());
        assertEquals(
                CombatAiAction.FLANK,
                controller.decide(redCavalry, LiveCombatController.BattlefieldView.exposedBackline(), layout).decision().action());
        assertEquals(
                CombatAiAction.SUPPORT,
                controller.decide(redMagic, LiveCombatController.BattlefieldView.supportedRear(), layout).decision().action());
    }

    private static LiveCombatUnit unit(CombatSide side, TroopType troopType) {
        return new LiveCombatUnit(
                UUID.randomUUID(),
                side,
                troopType,
                UUID.randomUUID(),
                null,
                "dev-test");
    }
}
