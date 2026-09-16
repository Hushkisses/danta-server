package kr.danta.paper.combat.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Dev114CombatAiValidationCommandTest {

    private final CombatAiValidationRuntime runtime = new CombatAiValidationRuntime();

    @Test
    void listsAllFiveProfilesInKorean() {
        String output = runtime.execute(new String[]{"profiles"});

        assertTrue(output.contains("보병"));
        assertTrue(output.contains("창병"));
        assertTrue(output.contains("궁병"));
        assertTrue(output.contains("기병"));
        assertTrue(output.contains("마법병"));
    }

    @Test
    void returnsKoreanDecisionForRepresentativeScenario() {
        String output = runtime.execute(new String[]{"decide", "spearmen", "cavalry-threat"});

        assertTrue(output.contains("창병"));
        assertTrue(output.contains("엄호/차단"));
    }

    @Test
    void magicSupportScenarioReportsSupportWithoutConcreteSpellEffect() {
        String output = runtime.execute(new String[]{"decide", "magic", "support"});

        assertTrue(output.contains("마법병"));
        assertTrue(output.contains("지원"));
        assertFalse(output.contains("화염구"));
        assertFalse(output.contains("마력석"));
    }

    @Test
    void invalidProfileReturnsSpecificKoreanMessage() {
        String output = runtime.execute(new String[]{"decide", "wizard", "support"});

        assertEquals("알 수 없는 병종입니다: wizard", output);
    }

    @Test
    void invalidScenarioReturnsSpecificKoreanMessage() {
        String output = runtime.execute(new String[]{"decide", "infantry", "unknown"});

        assertEquals("알 수 없는 전투 시나리오입니다: unknown", output);
    }

    @Test
    void missingArgumentsReturnsUsageInKorean() {
        assertEquals("사용법: /danta combat-ai profiles 또는 /danta combat-ai decide <병종> <시나리오>",
                runtime.execute(new String[]{}));
        assertEquals("사용법: /danta combat-ai decide <병종> <시나리오>",
                runtime.execute(new String[]{"decide", "infantry"}));
    }
}
