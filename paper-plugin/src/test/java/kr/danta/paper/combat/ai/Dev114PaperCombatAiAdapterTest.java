package kr.danta.paper.combat.ai;

import kr.danta.core.combat.TroopType;
import kr.danta.core.combat.ai.CombatAiAction;
import kr.danta.core.combat.ai.CombatAiObservation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Dev114PaperCombatAiAdapterTest {

    @Test
    void mapsAllSharedTroopTypesToMatchingProfiles() {
        PaperCombatAiRuntime runtime = new PaperCombatAiRuntime();

        assertEquals("INFANTRY", runtime.profileFor(TroopType.INFANTRY).name());
        assertEquals("SPEARMEN", runtime.profileFor(TroopType.SPEARMEN).name());
        assertEquals("ARCHERS", runtime.profileFor(TroopType.ARCHERS).name());
        assertEquals("CAVALRY", runtime.profileFor(TroopType.CAVALRY).name());
        assertEquals("MAGIC", runtime.profileFor(TroopType.MAGIC).name());
    }

    @Test
    void convertsPaperSnapshotIntoCoreObservationWithoutMinecraftObjects() {
        PaperCombatAiObservationFactory.TacticalSnapshot snapshot =
                new PaperCombatAiObservationFactory.TacticalSnapshot(
                        4.5,
                        TroopType.CAVALRY,
                        true,
                        true,
                        false,
                        false,
                        false,
                        false,
                        true);

        CombatAiObservation observation = new PaperCombatAiObservationFactory().from(snapshot);

        assertEquals(4.5, observation.nearestHostileDistance(), 0.000001);
        assertEquals(TroopType.CAVALRY, observation.nearestHostileType());
        assertTrue(observation.frontlineSupportPresent());
        assertTrue(observation.backlineThreatened());
        assertFalse(observation.exposedEnemyBackline());
        assertTrue(observation.alliedCombatGroupPresent());
    }

    @Test
    void delegatesToCoreControllerAndDoesNotInventMagicSpellEffects() {
        PaperCombatAiRuntime runtime = new PaperCombatAiRuntime();
        PaperCombatAiObservationFactory.TacticalSnapshot snapshot =
                new PaperCombatAiObservationFactory.TacticalSnapshot(
                        10.0,
                        TroopType.INFANTRY,
                        true,
                        false,
                        false,
                        false,
                        false,
                        false,
                        true);

        assertEquals(CombatAiAction.SUPPORT, runtime.decide(TroopType.MAGIC, snapshot).action());
    }
}
