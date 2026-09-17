package kr.danta.paper.combat.live;

import kr.danta.core.combat.TroopType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatAlliedSupportPolicyTest {

    @Test
    void magicUnitsDoNotCountOtherMagicUnitsAsACombatGroup() {
        LiveCombatUnit self = unit(CombatSide.RED, TroopType.MAGIC);
        LiveCombatUnit otherMagic = unit(CombatSide.RED, TroopType.MAGIC);

        assertFalse(CombatAlliedSupportPolicy.hasCombatGroup(self, List.of(self, otherMagic)));
    }

    @Test
    void magicUnitSupportsWhileAnyNonMagicAllyRemains() {
        LiveCombatUnit self = unit(CombatSide.RED, TroopType.MAGIC);
        LiveCombatUnit archer = unit(CombatSide.RED, TroopType.ARCHERS);
        LiveCombatUnit enemy = unit(CombatSide.BLUE, TroopType.INFANTRY);

        assertTrue(CombatAlliedSupportPolicy.hasCombatGroup(self, List.of(self, archer, enemy)));
    }

    private static LiveCombatUnit unit(CombatSide side, TroopType troopType) {
        return new LiveCombatUnit(
                UUID.randomUUID(), side, troopType,
                UUID.randomUUID(), null, "test");
    }
}
