package kr.danta.paper.combat.live;

import kr.danta.core.combat.LogicalForceAiMappingPolicy;
import kr.danta.core.combat.TroopType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class Dev119LiveMappedBattleResultTest {

    @Test
    void capturesSurvivingRepresentativesBySideAndTroopType() {
        LogicalForceAiMappingPolicy policy = new LogicalForceAiMappingPolicy(50);
        LogicalForceAiMappingPolicy.Mapping red = policy.map(Map.of(
                TroopType.INFANTRY, 120L,
                TroopType.ARCHERS, 60L));
        LogicalForceAiMappingPolicy.Mapping blue = policy.map(Map.of(
                TroopType.SPEARMEN, 90L));

        LiveMappedBattleResult result = LiveMappedBattleResult.capture(
                red,
                blue,
                List.of(
                        unit(CombatSide.RED, TroopType.INFANTRY),
                        unit(CombatSide.RED, TroopType.INFANTRY),
                        unit(CombatSide.RED, TroopType.ARCHERS),
                        unit(CombatSide.BLUE, TroopType.SPEARMEN)));

        assertSame(red, result.mapping(CombatSide.RED));
        assertSame(blue, result.mapping(CombatSide.BLUE));
        assertEquals(2, result.survivingAi(CombatSide.RED).get(TroopType.INFANTRY));
        assertEquals(1, result.survivingAi(CombatSide.RED).get(TroopType.ARCHERS));
        assertEquals(1, result.survivingAi(CombatSide.BLUE).get(TroopType.SPEARMEN));
    }

    @Test
    void resultDoesNotAssumeDevelopmentHundredToOneRatio() {
        LogicalForceAiMappingPolicy policy = new LogicalForceAiMappingPolicy(25);
        LogicalForceAiMappingPolicy.Mapping red = policy.map(Map.of(TroopType.CAVALRY, 51L));
        LogicalForceAiMappingPolicy.Mapping blue = policy.map(Map.of(TroopType.INFANTRY, 25L));

        LiveMappedBattleResult result = LiveMappedBattleResult.capture(
                red, blue,
                List.of(
                        unit(CombatSide.RED, TroopType.CAVALRY),
                        unit(CombatSide.RED, TroopType.CAVALRY),
                        unit(CombatSide.BLUE, TroopType.INFANTRY)));

        assertEquals(25, result.mapping(CombatSide.RED).logicalTroopsPerAi());
        assertEquals(2, result.survivingAi(CombatSide.RED).get(TroopType.CAVALRY));
    }

    private static LiveCombatUnit unit(CombatSide side, TroopType type) {
        return new LiveCombatUnit(
                UUID.randomUUID(), side, type, UUID.randomUUID(), null, "test");
    }
}
