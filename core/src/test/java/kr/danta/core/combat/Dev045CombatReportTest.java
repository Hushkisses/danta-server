package kr.danta.core.combat;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Dev045CombatReportTest {
    @Test void reportShowsOutcomeAndLossesInKorean() {
        CombatResult power = new CombatResolver().resolve(
                CombatSideInput.neutral("red", TroopType.INFANTRY, 1000),
                CombatSideInput.neutral("blue", TroopType.SPEARMEN, 1000));
        CombatReport report = CombatReport.from(new CombatLossPolicy().apply(power));
        List<String> lines = new CombatReportFormatter().formatKorean(report);

        assertEquals("[전투 보고서]", lines.get(0));
        assertEquals("red: 승리 | 전투 전 1000 | 손실 100 | 잔존 900", lines.get(1));
        assertEquals("blue: 패배 | 전투 전 1000 | 손실 250 | 잔존 750", lines.get(2));
        assertFalse(String.join(" ", lines).contains("VICTORY"));
        assertFalse(String.join(" ", lines).contains("DEFEAT"));
    }

    @Test void drawUsesKoreanDrawLabel() {
        CombatResult power = new CombatResolver().resolve(
                CombatSideInput.neutral("red", TroopType.INFANTRY, 100),
                CombatSideInput.neutral("blue", TroopType.INFANTRY, 100));
        CombatReport report = CombatReport.from(new CombatLossPolicy().apply(power));
        String rendered = String.join("\n", new CombatReportFormatter().formatKorean(report));
        assertTrue(rendered.contains("red: 무승부"));
        assertTrue(rendered.contains("blue: 무승부"));
    }
}
