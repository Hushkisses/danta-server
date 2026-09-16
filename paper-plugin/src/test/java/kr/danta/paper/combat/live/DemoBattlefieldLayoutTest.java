package kr.danta.paper.combat.live;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DemoBattlefieldLayoutTest {

    @Test
    void redAndBlueAdvanceTowardEachOtherWhileRetreatRoutesLeadAway() {
        DemoBattlefieldLayout layout = DemoBattlefieldLayout.around(100, 64, 100);

        double redAdvanceX = layout.routes(CombatSide.RED).advance().waypoints().getLast().x();
        double redRetreatX = layout.routes(CombatSide.RED).retreat().waypoints().getLast().x();
        double blueAdvanceX = layout.routes(CombatSide.BLUE).advance().waypoints().getLast().x();
        double blueRetreatX = layout.routes(CombatSide.BLUE).retreat().waypoints().getLast().x();

        assertTrue(redAdvanceX > redRetreatX);
        assertTrue(blueAdvanceX < blueRetreatX);
    }
}
