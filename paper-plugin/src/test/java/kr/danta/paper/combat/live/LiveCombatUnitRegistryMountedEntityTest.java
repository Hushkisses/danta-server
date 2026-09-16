package kr.danta.paper.combat.live;

import kr.danta.core.combat.TroopType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LiveCombatUnitRegistryMountedEntityTest {

    @Test
    void mountedUnitCanBeResolvedByRiderOrMountAndRemovalClearsBothIndexes() {
        LiveCombatUnitRegistry registry = new LiveCombatUnitRegistry();
        UUID unitId = UUID.randomUUID();
        UUID riderId = UUID.randomUUID();
        UUID mountId = UUID.randomUUID();
        LiveCombatUnit cavalry = new LiveCombatUnit(
                unitId,
                CombatSide.RED,
                TroopType.CAVALRY,
                riderId,
                mountId,
                "cavalry-dev");

        registry.register(cavalry);

        assertEquals(unitId, registry.byEntity(riderId).orElseThrow().unitId());
        assertEquals(unitId, registry.byEntity(mountId).orElseThrow().unitId());

        registry.remove(unitId);

        assertTrue(registry.byEntity(riderId).isEmpty());
        assertTrue(registry.byEntity(mountId).isEmpty());
    }
}
