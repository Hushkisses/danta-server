package kr.danta.paper.siege;

import kr.danta.core.siege.SiegeSide;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Dev118SiegePlayerCommandPolicyTest {

    @Test
    void parsesDevelopmentSideTokensWithoutExposingDomainEnumNamesToMessages() {
        assertEquals(SiegeSide.ATTACKER, SiegePlayerCommandPolicy.parseSide("attack"));
        assertEquals(SiegeSide.DEFENDER, SiegePlayerCommandPolicy.parseSide("defend"));
        assertEquals("공격", SiegePlayerCommandPolicy.sideText(SiegeSide.ATTACKER));
        assertEquals("방어", SiegePlayerCommandPolicy.sideText(SiegeSide.DEFENDER));
    }

    @Test
    void rejectsUnknownSideTokenWithKoreanReason() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> SiegePlayerCommandPolicy.parseSide("red"));
        assertTrue(error.getMessage().contains("attack 또는 defend"));
    }

    @Test
    void lethalDamageIsDetectedAtOrAboveCurrentHealth() {
        assertFalse(SiegePlayerCommandPolicy.lethal(20.0, 19.99));
        assertTrue(SiegePlayerCommandPolicy.lethal(20.0, 20.0));
        assertTrue(SiegePlayerCommandPolicy.lethal(4.0, 10.0));
    }
}
