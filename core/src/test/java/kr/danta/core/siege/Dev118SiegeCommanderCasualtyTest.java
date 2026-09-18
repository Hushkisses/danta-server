package kr.danta.core.siege;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class Dev118SiegeCommanderCasualtyTest {

    @Test
    void eliminatedCommanderCannotReenterSameSiegeUntilPointIsCleared() {
        SiegeCommanderCasualtyPolicy policy = SiegeCommanderCasualtyPolicy.developmentDefaults();
        SiegeParticipantRegistry registry = new SiegeParticipantRegistry(policy);
        UUID player = UUID.randomUUID();

        registry.join("red_capital", player, SiegeSide.ATTACKER);
        SiegeCommanderCasualty casualty = registry.eliminate("red_capital", player);

        assertTrue(registry.eliminated("red_capital", player));
        assertEquals(-20, casualty.moraleDelta());
        assertFalse(casualty.commanderBonusActive());

        IllegalStateException rejected = assertThrows(
                IllegalStateException.class,
                () -> registry.join("red_capital", player, SiegeSide.ATTACKER));
        assertTrue(rejected.getMessage().contains("해당 공성전에서는 다시 참전할 수 없습니다"));

        registry.clear("red_capital");
        assertDoesNotThrow(() -> registry.join("red_capital", player, SiegeSide.ATTACKER));
    }

    @Test
    void casualtyPenaltyAccumulatesPerSideButNotForDuplicateElimination() {
        SiegeParticipantRegistry registry = new SiegeParticipantRegistry(new SiegeCommanderCasualtyPolicy(20));
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        registry.join("red_capital", first, SiegeSide.DEFENDER);
        registry.join("red_capital", second, SiegeSide.DEFENDER);
        registry.eliminate("red_capital", first);
        registry.eliminate("red_capital", second);

        assertEquals(-40, registry.moraleDelta("red_capital", SiegeSide.DEFENDER));
        assertThrows(IllegalStateException.class, () -> registry.eliminate("red_capital", first));
        assertEquals(-40, registry.moraleDelta("red_capital", SiegeSide.DEFENDER));
    }

    @Test
    void participantOnOtherPointIsIndependent() {
        SiegeParticipantRegistry registry = new SiegeParticipantRegistry(new SiegeCommanderCasualtyPolicy(20));
        UUID player = UUID.randomUUID();

        registry.join("red_capital", player, SiegeSide.ATTACKER);
        registry.eliminate("red_capital", player);

        assertFalse(registry.eliminated("canyon_fort", player));
    }
}
