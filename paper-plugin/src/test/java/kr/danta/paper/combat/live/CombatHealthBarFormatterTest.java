package kr.danta.paper.combat.live;

import kr.danta.core.combat.TroopType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CombatHealthBarFormatterTest {
    @Test
    void formatsSideRoleNumericHealthAndTenSegmentBar() {
        assertEquals(
                "[적] 보병  ♥ 14/20  ███████░░░",
                CombatHealthBarFormatter.format(CombatSide.RED, TroopType.INFANTRY, 14.0, 20.0));
    }

    @Test
    void clampsHealthAndKeepsOneSegmentForLivingUnit() {
        assertEquals(
                "[청] 궁병  ♥ 1/20  █░░░░░░░░░",
                CombatHealthBarFormatter.format(CombatSide.BLUE, TroopType.ARCHERS, 0.2, 20.0));
        assertEquals(
                "[청] 궁병  ♥ 0/20  ░░░░░░░░░░",
                CombatHealthBarFormatter.format(CombatSide.BLUE, TroopType.ARCHERS, 0.0, 20.0));
    }
}
