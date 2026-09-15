package kr.danta.core.economy;

import kr.danta.core.nation.NationState;
import kr.danta.core.state.GameState;

import java.util.Objects;

/**
 * DEV-050 transfer boundary.
 * Design rule: personal -> treasury contribution is allowed; treasury -> personal direct withdrawal is forbidden.
 */
public final class EconomyTransferService {
    private final GameState gameState;

    public EconomyTransferService(GameState gameState) {
        this.gameState = Objects.requireNonNull(gameState, "gameState");
    }

    public ContributionResult contributeToTreasury(String playerId, String nationId, long amount) {
        if (amount <= 0L) throw new IllegalArgumentException("amount must be > 0");
        PersonalWallet wallet = gameState.personalWallet(playerId)
                .orElseThrow(() -> new IllegalArgumentException("personal wallet does not exist: " + playerId));
        NationState nation = gameState.nation(nationId)
                .orElseThrow(() -> new IllegalArgumentException("nation does not exist: " + nationId));
        synchronized (this) {
            if (!wallet.tryWithdraw(amount)) return new ContributionResult(false, wallet.balance(), nation.treasury());
            try {
                nation.deposit(amount);
            } catch (RuntimeException ex) {
                wallet.deposit(amount);
                throw ex;
            }
            return new ContributionResult(true, wallet.balance(), nation.treasury());
        }
    }

    /** Explicitly rejects the prohibited direction instead of exposing a treasury-to-wallet transfer API. */
    public void withdrawTreasuryToPersonal(String nationId, String playerId, long amount) {
        throw new UnsupportedOperationException("direct treasury-to-personal withdrawal is prohibited");
    }

    public record ContributionResult(boolean transferred, long personalBalance, long treasuryBalance) {}
}
