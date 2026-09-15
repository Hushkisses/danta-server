package kr.danta.core.combat;

/**
 * Execution-plan DEV-042 temporary minimum casualty/retreat policy.
 * Balance placeholder approved for later replacement: winner 10%, loser 25%.
 */
public final class CombatLossPolicy {
    public static final double WINNER_LOSS_RATE = 0.10;
    public static final double LOSER_LOSS_RATE = 0.25;
    public static final double DRAW_LOSS_RATE = 0.10;

    public CombatResolution apply(CombatResult result) {
        if (result.draw()) {
            return new CombatResolution(result,
                    loss(result.first(), CombatOutcome.DRAW, DRAW_LOSS_RATE, false),
                    loss(result.second(), CombatOutcome.DRAW, DRAW_LOSS_RATE, false));
        }

        String winner = result.winner().orElseThrow();
        boolean firstWins = result.first().sideId().equals(winner);
        return new CombatResolution(result,
                loss(result.first(), firstWins ? CombatOutcome.VICTORY : CombatOutcome.DEFEAT,
                        firstWins ? WINNER_LOSS_RATE : LOSER_LOSS_RATE, !firstWins),
                loss(result.second(), firstWins ? CombatOutcome.DEFEAT : CombatOutcome.VICTORY,
                        firstWins ? LOSER_LOSS_RATE : WINNER_LOSS_RATE, firstWins));
    }

    private CombatLossResult loss(CombatSideResult side, CombatOutcome outcome, double rate, boolean retreat) {
        int losses = Math.min(side.initialTroops(), (int) Math.round(side.initialTroops() * rate));
        return new CombatLossResult(side.sideId(), outcome, side.initialTroops(), losses,
                side.initialTroops() - losses, retreat);
    }
}
