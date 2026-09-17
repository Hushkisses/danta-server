package kr.danta.paper.combat.live;

import kr.danta.core.combat.TroopType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatAttackPolicyTest {

    @Test
    void attackModesMatchDevelopmentRoles() {
        CombatAttackPolicy policy = CombatAttackPolicy.developmentDefaults();

        assertEquals(CombatAttackPolicy.AttackMode.MELEE, policy.forType(TroopType.INFANTRY).mode());
        assertEquals(CombatAttackPolicy.AttackMode.MELEE, policy.forType(TroopType.SPEARMEN).mode());
        assertEquals(CombatAttackPolicy.AttackMode.RANGED, policy.forType(TroopType.ARCHERS).mode());
        assertEquals(CombatAttackPolicy.AttackMode.MELEE, policy.forType(TroopType.CAVALRY).mode());
        assertEquals(CombatAttackPolicy.AttackMode.RANGED, policy.forType(TroopType.MAGIC).mode());
    }

    @Test
    void magicHasTemporaryDamagingRangedAttackSoEndgameCanResolve() {
        CombatAttackPolicy.AttackSpec magic = CombatAttackPolicy.developmentDefaults().forType(TroopType.MAGIC);

        assertEquals(9.0, magic.range());
        assertTrue(magic.damage() > 0.0);
        assertTrue(magic.cooldownMillis() > 0L);
    }
}
