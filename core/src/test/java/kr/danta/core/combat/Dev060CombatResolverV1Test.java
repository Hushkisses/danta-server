package kr.danta.core.combat;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Dev060CombatResolverV1Test {
    private final CombatResolverV1 resolver = new CombatResolverV1();

    @Test void exposesAllDesignPhasesInOrder() {
        CombatV1Result result = resolver.resolve(
                CombatSideInput.neutral("red", TroopType.INFANTRY, 1000),
                CombatSideInput.neutral("blue", TroopType.SPEARMEN, 1000));

        assertEquals(List.of(CombatPhase.values()),
                result.phases().stream().map(CombatPhaseResult::phase).toList());
    }

    @Test void dev060NeutralPhasesPreserveExistingCombatBaseline() {
        CombatSideInput red = CombatSideInput.neutral("red", TroopType.INFANTRY, 1000);
        CombatSideInput blue = CombatSideInput.neutral("blue", TroopType.SPEARMEN, 1000);

        CombatResult v0 = new CombatResolver().resolve(red, blue);
        CombatV1Result v1 = resolver.resolve(red, blue);

        assertEquals(v0.first().effectivePower(), v1.finalResult().first().effectivePower(), 0.000001);
        assertEquals(v0.second().effectivePower(), v1.finalResult().second().effectivePower(), 0.000001);
        assertEquals(v0.winnerSideId(), v1.finalResult().winnerSideId());
        assertTrue(v1.phases().stream().allMatch(p ->
                p.firstMultiplier() == 1.0 && p.secondMultiplier() == 1.0));
    }

    @Test void equalSidesRemainDrawThroughNeutralPipeline() {
        CombatV1Result result = resolver.resolve(
                CombatSideInput.neutral("red", TroopType.CAVALRY, 500),
                CombatSideInput.neutral("blue", TroopType.CAVALRY, 500));
        assertTrue(result.finalResult().draw());
    }
}
