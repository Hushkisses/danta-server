package kr.danta.paper.combat.live;

import kr.danta.core.combat.TroopType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CombatAttackPolicyTest {

    @Test
    void attackModesMatchDevelopmentRoles() {
        CombatAttackPolicy policy = CombatAttackPolicy.developmentDefaults();

        assertEquals(CombatAttackPolicy.AttackMode.MELEE, policy.forType(TroopType.INFANTRY).mode());
        assertEquals(CombatAttackPolicy.AttackMode.MELEE, policy.forType(TroopType.SPEARMEN).mode());
        assertEquals(CombatAttackPolicy.AttackMode.RANGED, policy.forType(TroopType.ARCHERS).mode());
        assertEquals(CombatAttackPolicy.AttackMode.MELEE, policy.forType(TroopType.CAVALRY).mode());
        assertEquals(CombatAttackPolicy.AttackMode.SUPPORT_VISUAL, policy.forType(TroopType.MAGIC).mode());
    }
}
