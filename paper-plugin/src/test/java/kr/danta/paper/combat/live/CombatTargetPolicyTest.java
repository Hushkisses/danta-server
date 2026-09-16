package kr.danta.paper.combat.live;

import kr.danta.core.combat.TroopType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CombatTargetPolicyTest {

    @Test
    void onlyOpposingTrackedUnitsAreEligibleTargets() {
        CombatTargetPolicy policy = new CombatTargetPolicy();
        LiveCombatUnit red = unit(CombatSide.RED, TroopType.INFANTRY);
        LiveCombatUnit redFriend = unit(CombatSide.RED, TroopType.ARCHERS);
        LiveCombatUnit blue = unit(CombatSide.BLUE, TroopType.SPEARMEN);

        assertFalse(policy.mayTarget(red, red));
        assertFalse(policy.mayTarget(red, redFriend));
        assertTrue(policy.mayTarget(red, blue));
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
