package kr.danta.core.combat;

public record CombatLossResult(
        String sideId,
        CombatOutcome outcome,
        int initialTroops,
        int losses,
        int remainingTroops,
        boolean retreatRequired
) {
    public CombatLossResult {
        if (initialTroops <= 0) throw new IllegalArgumentException("initialTroops must be positive");
        if (losses < 0 || losses > initialTroops) throw new IllegalArgumentException("invalid losses");
        if (remainingTroops != initialTroops - losses) throw new IllegalArgumentException("remainingTroops mismatch");
    }
}
