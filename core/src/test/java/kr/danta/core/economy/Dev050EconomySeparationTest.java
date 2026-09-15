package kr.danta.core.economy;

import kr.danta.core.nation.NationState;
import kr.danta.core.state.GameState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Dev050EconomySeparationTest {
    @Test void personalContributionMovesMoneyOnlyTowardTreasury() {
        GameState state = new GameState();
        state.addNation(new NationState("red", "적국", null, 500, kr.danta.core.nation.NationStatus.ACTIVE));
        state.addPersonalWallet(new PersonalWallet("player-1", 1000));
        EconomyTransferService service = new EconomyTransferService(state);

        var result = service.contributeToTreasury("player-1", "red", 300);
        assertTrue(result.transferred());
        assertEquals(700, state.personalWallet("player-1").orElseThrow().balance());
        assertEquals(800, state.nation("red").orElseThrow().treasury());
    }

    @Test void insufficientPersonalBalanceDoesNotPartiallyMutateEitherSide() {
        GameState state = new GameState();
        state.addNation(new NationState("red", "적국"));
        state.addPersonalWallet(new PersonalWallet("player-1", 50));
        var result = new EconomyTransferService(state).contributeToTreasury("player-1", "red", 100);
        assertFalse(result.transferred());
        assertEquals(50, state.personalWallet("player-1").orElseThrow().balance());
        assertEquals(0, state.nation("red").orElseThrow().treasury());
    }

    @Test void directTreasuryToPersonalWithdrawalIsExplicitlyForbidden() {
        GameState state = new GameState();
        EconomyTransferService service = new EconomyTransferService(state);
        assertThrows(UnsupportedOperationException.class,
                () -> service.withdrawTreasuryToPersonal("red", "player-1", 1));
    }
}
