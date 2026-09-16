package kr.danta.paper.combat.live;

import kr.danta.core.combat.TroopType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CombatUnitVisualCatalogTest {

    @Test
    void everyTroopTypeHasAReplaceableVisualProfile() {
        CombatUnitVisualCatalog catalog = CombatUnitVisualCatalog.developmentDefaults();

        for (TroopType type : TroopType.values()) {
            CombatUnitVisualProfile profile = catalog.profileFor(type);
            assertNotNull(profile);
            assertFalse(profile.profileId().isBlank());
        }

        assertNotEquals(
                TroopType.INFANTRY.name(),
                catalog.profileFor(TroopType.INFANTRY).profileId());
    }
}
