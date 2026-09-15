package kr.danta.core.combat;

import java.util.Objects;

/** DEV-045 immutable minimum combat report: result and losses only. */
public record CombatReport(
        String firstSideId,
        String secondSideId,
        CombatOutcome firstOutcome,
        CombatOutcome secondOutcome,
        int firstInitialTroops,
        int secondInitialTroops,
        int firstLosses,
        int secondLosses,
        int firstRemainingTroops,
        int secondRemainingTroops
) {
    public CombatReport {
        Objects.requireNonNull(firstSideId, "firstSideId");
        Objects.requireNonNull(secondSideId, "secondSideId");
        Objects.requireNonNull(firstOutcome, "firstOutcome");
        Objects.requireNonNull(secondOutcome, "secondOutcome");
    }

    public static CombatReport from(CombatResolution resolution) {
        Objects.requireNonNull(resolution, "resolution");
        CombatLossResult first = resolution.first();
        CombatLossResult second = resolution.second();
        return new CombatReport(first.sideId(), second.sideId(), first.outcome(), second.outcome(),
                first.initialTroops(), second.initialTroops(), first.losses(), second.losses(),
                first.remainingTroops(), second.remainingTroops());
    }
}
