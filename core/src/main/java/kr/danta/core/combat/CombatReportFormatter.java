package kr.danta.core.combat;

import java.util.List;
import java.util.Objects;

/**
 * Korean player-facing DEV-045 report formatter.
 * Internal enum/IDs stay English; outcome labels exposed to players are Korean.
 */
public final class CombatReportFormatter {
    public List<String> formatKorean(CombatReport report) {
        Objects.requireNonNull(report, "report");
        return List.of(
                "[전투 보고서]",
                side(report.firstSideId(), report.firstOutcome(), report.firstInitialTroops(),
                        report.firstLosses(), report.firstRemainingTroops()),
                side(report.secondSideId(), report.secondOutcome(), report.secondInitialTroops(),
                        report.secondLosses(), report.secondRemainingTroops())
        );
    }

    private static String side(String id, CombatOutcome outcome, int initial, int losses, int remaining) {
        return id + ": " + outcomeKorean(outcome)
                + " | 전투 전 " + initial
                + " | 손실 " + losses
                + " | 잔존 " + remaining;
    }

    private static String outcomeKorean(CombatOutcome outcome) {
        return switch (outcome) {
            case VICTORY -> "승리";
            case DEFEAT -> "패배";
            case DRAW -> "무승부";
        };
    }
}
