package kr.danta.paper.combat.live;

import kr.danta.core.combat.TroopType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CombatTargetSelectionPolicyTest {
    private static final UUID INFANTRY = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID SPEARMEN = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID ARCHERS = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID CAVALRY = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final UUID MAGIC = UUID.fromString("00000000-0000-0000-0000-000000000005");

    @Test
    void cavalryPrioritizesSpearmenThenArchersThenMagicThenInfantry() {
        CombatTargetSelectionPolicy policy = new CombatTargetSelectionPolicy(28.0, new Random(1));
        List<CombatTargetSelectionPolicy.Candidate> all = List.of(
                candidate(INFANTRY, TroopType.INFANTRY, 3.0),
                candidate(MAGIC, TroopType.MAGIC, 4.0),
                candidate(ARCHERS, TroopType.ARCHERS, 5.0),
                candidate(SPEARMEN, TroopType.SPEARMEN, 20.0));

        assertEquals(SPEARMEN, policy.select(TroopType.CAVALRY, all, 3.0).orElseThrow());
        assertEquals(ARCHERS, policy.select(TroopType.CAVALRY,
                all.stream().filter(c -> !c.unitId().equals(SPEARMEN)).toList(), 3.0).orElseThrow());
        assertEquals(MAGIC, policy.select(TroopType.CAVALRY,
                all.stream().filter(c -> !c.unitId().equals(SPEARMEN) && !c.unitId().equals(ARCHERS)).toList(), 3.0).orElseThrow());
    }

    @Test
    void infantryPrefersInfantryOrSpearmenOverOtherTargets() {
        CombatTargetSelectionPolicy policy = new CombatTargetSelectionPolicy(28.0, new Random(1));
        List<CombatTargetSelectionPolicy.Candidate> candidates = List.of(
                candidate(ARCHERS, TroopType.ARCHERS, 2.0),
                candidate(SPEARMEN, TroopType.SPEARMEN, 7.0),
                candidate(INFANTRY, TroopType.INFANTRY, 5.0));

        assertEquals(INFANTRY, policy.select(TroopType.INFANTRY, candidates, 2.8).orElseThrow());
    }

    @Test
    void spearmenPreferCavalryEvenWhenOtherEnemyIsCloser() {
        CombatTargetSelectionPolicy policy = new CombatTargetSelectionPolicy(28.0, new Random(1));
        List<CombatTargetSelectionPolicy.Candidate> candidates = List.of(
                candidate(INFANTRY, TroopType.INFANTRY, 2.0),
                candidate(CAVALRY, TroopType.CAVALRY, 18.0));

        assertEquals(CAVALRY, policy.select(TroopType.SPEARMEN, candidates, 3.2).orElseThrow());
    }

    @Test
    void cavalryAndSpearmenDetectEachOtherAcrossExtendedFlankRadius() {
        CombatTargetSelectionPolicy policy = new CombatTargetSelectionPolicy(28.0, 40.0, new Random(1));

        assertEquals(SPEARMEN, policy.select(TroopType.CAVALRY, List.of(
                candidate(INFANTRY, TroopType.INFANTRY, 5.0),
                candidate(SPEARMEN, TroopType.SPEARMEN, 36.0)), 3.0).orElseThrow());

        assertEquals(CAVALRY, policy.select(TroopType.SPEARMEN, List.of(
                candidate(INFANTRY, TroopType.INFANTRY, 2.0),
                candidate(CAVALRY, TroopType.CAVALRY, 36.0)), 3.2).orElseThrow());
    }

    @Test
    void extendedFlankRadiusDoesNotPullOtherTargetsFromTooFarAway() {
        CombatTargetSelectionPolicy policy = new CombatTargetSelectionPolicy(28.0, 40.0, new Random(1));
        List<CombatTargetSelectionPolicy.Candidate> candidates = List.of(
                candidate(SPEARMEN, TroopType.SPEARMEN, 41.0),
                candidate(ARCHERS, TroopType.ARCHERS, 10.0));

        assertEquals(ARCHERS, policy.select(TroopType.CAVALRY, candidates, 3.0).orElseThrow());
    }

    @Test
    void archersChooseOnlyFromEnemiesInsideAttackRangeWhenAnyAreAvailable() {
        CombatTargetSelectionPolicy policy = new CombatTargetSelectionPolicy(28.0, new Random(2));
        List<CombatTargetSelectionPolicy.Candidate> candidates = List.of(
                candidate(INFANTRY, TroopType.INFANTRY, 8.0),
                candidate(SPEARMEN, TroopType.SPEARMEN, 12.0),
                candidate(CAVALRY, TroopType.CAVALRY, 16.0));

        UUID selected = policy.select(TroopType.ARCHERS, candidates, 14.0).orElseThrow();
        assertTrue(selected.equals(INFANTRY) || selected.equals(SPEARMEN));
        assertNotEquals(CAVALRY, selected);
    }

    @Test
    void archersAcquireNearestEnemyWithinSearchRadiusWhenNothingIsInAttackRange() {
        CombatTargetSelectionPolicy policy = new CombatTargetSelectionPolicy(28.0, new Random(2));
        List<CombatTargetSelectionPolicy.Candidate> candidates = List.of(
                candidate(MAGIC, TroopType.MAGIC, 20.0),
                candidate(CAVALRY, TroopType.CAVALRY, 24.0),
                candidate(INFANTRY, TroopType.INFANTRY, 31.0));

        assertEquals(MAGIC, policy.select(TroopType.ARCHERS, candidates, 14.0).orElseThrow());
    }

    @Test
    void meleePriorityCandidatesOutsideSearchRadiusAreIgnored() {
        CombatTargetSelectionPolicy policy = new CombatTargetSelectionPolicy(28.0, new Random(1));
        List<CombatTargetSelectionPolicy.Candidate> candidates = List.of(
                candidate(SPEARMEN, TroopType.SPEARMEN, 30.0),
                candidate(ARCHERS, TroopType.ARCHERS, 10.0));

        assertEquals(ARCHERS, policy.select(TroopType.CAVALRY, candidates, 3.0).orElseThrow());
    }

    private static CombatTargetSelectionPolicy.Candidate candidate(UUID id, TroopType type, double distance) {
        return new CombatTargetSelectionPolicy.Candidate(id, type, distance);
    }
}
