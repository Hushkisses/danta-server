package kr.danta.paper.combat.live;

import kr.danta.core.combat.TroopType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LiveCombatUnitRegistryTest {

    @Test
    void registerLookupRemoveAndClearAreDeterministic() {
        LiveCombatUnitRegistry registry = new LiveCombatUnitRegistry();
        LiveCombatUnit unit = new LiveCombatUnit(
                UUID.randomUUID(),
                CombatSide.RED,
                TroopType.INFANTRY,
                UUID.randomUUID(),
                null,
                "dev_infantry");

        registry.register(unit);
        assertEquals(unit, registry.byPrimaryEntity(unit.primaryEntityId()).orElseThrow());
        assertEquals(1, registry.units().size());

        registry.remove(unit.unitId());
        assertTrue(registry.units().isEmpty());
        assertTrue(registry.byPrimaryEntity(unit.primaryEntityId()).isEmpty());

        LiveCombatUnit second = new LiveCombatUnit(
                UUID.randomUUID(),
                CombatSide.BLUE,
                TroopType.ARCHERS,
                UUID.randomUUID(),
                null,
                "dev_archers");
        registry.register(second);
        registry.clear();
        assertTrue(registry.units().isEmpty());
    }
}
