package kr.danta.core.general;

import kr.danta.core.combat.TroopType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Dev072GeneralTroopSynergyTest {
    @Test void troopSpecificSynergyAppliesOnlyToTarget() {
        GeneralTroopSynergy synergy = GeneralTroopSynergy.troopType(TroopType.ARCHERS);
        assertTrue(synergy.appliesTo(TroopType.ARCHERS));
        assertFalse(synergy.appliesTo(TroopType.INFANTRY));
        assertEquals(TroopType.ARCHERS, synergy.targetedTroopType().orElseThrow());
    }

    @Test void wholeArmySynergyAppliesToEveryCurrentBasicTroopType() {
        GeneralTroopSynergy synergy = GeneralTroopSynergy.allTroops();
        for (TroopType type : TroopType.values()) assertTrue(synergy.appliesTo(type));
        assertTrue(synergy.targetedTroopType().isEmpty());
    }

    @Test void generalCanOwnOptionalSynergyWithoutInventedBonusRate() {
        GeneralState general = new GeneralState("g1", "red", GeneralGrade.B, 4);
        assertTrue(general.troopSynergy().isEmpty());

        GeneralTroopSynergy synergy = GeneralTroopSynergy.troopType(TroopType.CAVALRY);
        general.setTroopSynergy(synergy);
        assertEquals(synergy, general.troopSynergy().orElseThrow());

        general.clearTroopSynergy();
        assertTrue(general.troopSynergy().isEmpty());
    }

    @Test void synergyContractContainsNoNumericMultiplier() {
        assertArrayEquals(new String[]{"scope", "troopType"},
                java.util.Arrays.stream(GeneralTroopSynergy.class.getRecordComponents())
                        .map(java.lang.reflect.RecordComponent::getName).toArray(String[]::new));
    }
}
