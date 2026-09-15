package kr.danta.core.general;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Dev076GeneralAbilityCatalogTest {
    @Test void everyShippedEliteAbilityHasExactlyOneSemanticHook() throws Exception {
        List<GeneralDefinition> defs;
        try (var in = getClass().getResourceAsStream("/generals/initial-generals.yml")) {
            assertNotNull(in);
            defs = new GeneralCatalogLoader().load(in);
        }
        assertEquals(10, defs.size());
        for (GeneralDefinition definition : defs) {
            assertEquals(1, definition.abilityIds().size());
            String abilityId = definition.abilityIds().getFirst();
            assertTrue(GeneralAbilityCatalog.find(abilityId).isPresent(), abilityId);
        }
    }

    @Test void civilAbilitiesTargetExistingEconomyAdministrationBoundaries() {
        assertEquals(GeneralAbilityHook.POINT_GOLD_REVENUE,
                GeneralAbilityCatalog.find("prosperous_domain").orElseThrow().hook());
        assertEquals(GeneralAbilityHook.POINT_STRATEGIC_RESOURCE_PRODUCTION,
                GeneralAbilityCatalog.find("production_management").orElseThrow().hook());
        assertEquals(GeneralAbilityHook.NATION_ADMINISTRATIVE_CAPACITY,
                GeneralAbilityCatalog.find("grand_reform").orElseThrow().hook());
        assertEquals(GeneralAbilityHook.ARMY_SUPPLY_EFFICIENCY,
                GeneralAbilityCatalog.find("supply_mastery").orElseThrow().hook());
    }

    @Test void catalogDoesNotInventNumericBalanceCoefficients() {
        assertEquals(2, GeneralAbilityEffect.class.getRecordComponents().length);
    }
}
