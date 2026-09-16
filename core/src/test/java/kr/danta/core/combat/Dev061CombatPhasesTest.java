package kr.danta.core.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Dev061CombatPhasesTest {
    private final CombatResolverV1 resolver = new CombatResolverV1();

    @Test void infantryCounterAgainstSpearmenIsAppliedAtFrontline() {
        CombatV1Result result = resolver.resolve(
                CombatSideInput.neutral("red", TroopType.INFANTRY, 1000),
                CombatSideInput.neutral("blue", TroopType.SPEARMEN, 1000));

        assertEquals(1.0, phase(result, CombatPhase.OPENING).firstMultiplier());
        assertEquals(1.25, phase(result, CombatPhase.FRONTLINE).firstMultiplier());
        assertEquals(1.0, phase(result, CombatPhase.BACKLINE).firstMultiplier());
        assertEquals(1.0, phase(result, CombatPhase.MOBILE).firstMultiplier());
        assertEquals(1250.0, result.finalResult().first().effectivePower(), 0.000001);
        assertEquals("red", result.finalResult().winner().orElseThrow());
    }

    @Test void archerCounterAgainstInfantryIsAppliedAtBackline() {
        CombatV1Result result = resolver.resolve(
                CombatSideInput.neutral("red", TroopType.ARCHERS, 1000),
                CombatSideInput.neutral("blue", TroopType.INFANTRY, 1000));

        assertEquals(1.25, phase(result, CombatPhase.BACKLINE).firstMultiplier());
        assertEquals(1.0, phase(result, CombatPhase.FRONTLINE).firstMultiplier());
        assertEquals(1250.0, result.finalResult().first().effectivePower(), 0.000001);
    }

    @Test void cavalryCounterAgainstArchersIsAppliedAtMobile() {
        CombatV1Result result = resolver.resolve(
                CombatSideInput.neutral("red", TroopType.CAVALRY, 1000),
                CombatSideInput.neutral("blue", TroopType.ARCHERS, 1000));

        assertEquals(1.25, phase(result, CombatPhase.MOBILE).firstMultiplier());
        assertEquals(1.0, phase(result, CombatPhase.BACKLINE).firstMultiplier());
        assertEquals(1250.0, result.finalResult().first().effectivePower(), 0.000001);
    }

    @Test void magicSupportTypeUsesBacklinePhase() {
        assertEquals(CombatPhase.BACKLINE, CombatPhasePolicy.phaseFor(TroopType.MAGIC));
    }

    @Test void finalResultsRemainRegressionCompatibleWithV0() {
        CombatSideInput red = new CombatSideInput("red", TroopType.SPEARMEN, 900, 1.1, 1.2, 0.9, 0.8, 1.05);
        CombatSideInput blue = new CombatSideInput("blue", TroopType.CAVALRY, 1000, 0.9, 1.0, 1.1, 1.0, 0.95);

        CombatResult oldResult = new CombatResolver().resolve(red, blue);
        CombatResult newResult = resolver.resolve(red, blue).finalResult();

        assertEquals(oldResult.first().effectivePower(), newResult.first().effectivePower(), 0.000001);
        assertEquals(oldResult.second().effectivePower(), newResult.second().effectivePower(), 0.000001);
        assertEquals(oldResult.winnerSideId(), newResult.winnerSideId());
    }

    @Test void moraleAndRetreatPursuitRemainNeutralForLaterTickets() {
        CombatV1Result result = resolver.resolve(
                CombatSideInput.neutral("red", TroopType.CAVALRY, 1000),
                CombatSideInput.neutral("blue", TroopType.ARCHERS, 1000));
        assertEquals(1.0, phase(result, CombatPhase.MORALE).firstMultiplier());
        assertEquals(1.0, phase(result, CombatPhase.MORALE).secondMultiplier());
        assertEquals(1.0, phase(result, CombatPhase.RETREAT_PURSUIT).firstMultiplier());
        assertEquals(1.0, phase(result, CombatPhase.RETREAT_PURSUIT).secondMultiplier());
    }

    private CombatPhaseResult phase(CombatV1Result result, CombatPhase phase) {
        return result.phases().stream().filter(p -> p.phase() == phase).findFirst().orElseThrow();
    }
}
