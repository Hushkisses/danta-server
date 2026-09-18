package kr.danta.core.combat;

import kr.danta.core.army.ArmyState;
import kr.danta.core.army.ArmyStatus;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class Dev119LogicalArmyCasualtyMergeTest {

    @Test
    void mergesAsymmetricLossesPerTroopType() {
        ArmyState army = new ArmyState(
                "army_a", "red", "red_capital", ArmyStatus.STATIONED,
                Map.of(
                        TroopType.INFANTRY, 250L,
                        TroopType.ARCHERS, 80L));
        LogicalForceAiMappingPolicy policy = new LogicalForceAiMappingPolicy(100);
        LogicalForceAiMappingPolicy.Mapping mapping = policy.map(army.troopComposition());

        LogicalArmyCasualtyMergeResult result = new LogicalArmyCasualtyMergeService(policy).merge(
                army, mapping,
                Map.of(
                        TroopType.INFANTRY, 2,
                        TroopType.ARCHERS, 0));

        assertEquals(167L, army.troopCount(TroopType.INFANTRY));
        assertEquals(0L, army.troopCount(TroopType.ARCHERS));
        assertEquals(167L, army.totalTroops());
        assertEquals(83L, result.losses().get(TroopType.INFANTRY));
        assertEquals(80L, result.losses().get(TroopType.ARCHERS));
        assertEquals(163L, result.totalLoss());
    }

    @Test
    void fullSurvivalPreservesExactLogicalComposition() {
        ArmyState army = new ArmyState(
                "army_a", "red", "red_capital", ArmyStatus.STATIONED,
                Map.of(TroopType.CAVALRY, 137L));
        LogicalForceAiMappingPolicy policy = new LogicalForceAiMappingPolicy(100);
        LogicalForceAiMappingPolicy.Mapping mapping = policy.map(army.troopComposition());

        LogicalArmyCasualtyMergeResult result = new LogicalArmyCasualtyMergeService(policy).merge(
                army, mapping, Map.of(TroopType.CAVALRY, 2));

        assertEquals(137L, army.troopCount(TroopType.CAVALRY));
        assertEquals(0L, result.totalLoss());
    }

    @Test
    void zeroSurvivorsZeroOnlyThatTroopType() {
        ArmyState army = new ArmyState(
                "army_a", "red", "red_capital", ArmyStatus.STATIONED,
                Map.of(TroopType.SPEARMEN, 50L, TroopType.MAGIC, 20L));
        LogicalForceAiMappingPolicy policy = new LogicalForceAiMappingPolicy(100);
        LogicalForceAiMappingPolicy.Mapping mapping = policy.map(army.troopComposition());

        new LogicalArmyCasualtyMergeService(policy).merge(
                army, mapping,
                Map.of(TroopType.SPEARMEN, 0, TroopType.MAGIC, 1));

        assertEquals(0L, army.troopCount(TroopType.SPEARMEN));
        assertEquals(20L, army.troopCount(TroopType.MAGIC));
    }

    @Test
    void staleMappingIsRejectedWithoutMutation() {
        ArmyState army = new ArmyState(
                "army_a", "red", "red_capital", ArmyStatus.STATIONED,
                Map.of(TroopType.INFANTRY, 200L));
        LogicalForceAiMappingPolicy policy = new LogicalForceAiMappingPolicy(100);
        LogicalForceAiMappingPolicy.Mapping stale = policy.map(army.troopComposition());

        army.replaceTroopComposition(Map.of(TroopType.INFANTRY, 150L));

        assertThrows(IllegalStateException.class,
                () -> new LogicalArmyCasualtyMergeService(policy).merge(
                        army, stale, Map.of(TroopType.INFANTRY, 1)));

        assertEquals(150L, army.troopCount(TroopType.INFANTRY));
        assertEquals(150L, army.totalTroops());
    }

    @Test
    void invalidSurvivorCountDoesNotPartiallyMutateArmy() {
        ArmyState army = new ArmyState(
                "army_a", "red", "red_capital", ArmyStatus.STATIONED,
                Map.of(TroopType.INFANTRY, 100L, TroopType.ARCHERS, 100L));
        LogicalForceAiMappingPolicy policy = new LogicalForceAiMappingPolicy(100);
        LogicalForceAiMappingPolicy.Mapping mapping = policy.map(army.troopComposition());

        assertThrows(IllegalArgumentException.class,
                () -> new LogicalArmyCasualtyMergeService(policy).merge(
                        army, mapping,
                        Map.of(TroopType.INFANTRY, 2, TroopType.ARCHERS, 1)));

        assertEquals(100L, army.troopCount(TroopType.INFANTRY));
        assertEquals(100L, army.troopCount(TroopType.ARCHERS));
        assertEquals(200L, army.totalTroops());
    }
}
