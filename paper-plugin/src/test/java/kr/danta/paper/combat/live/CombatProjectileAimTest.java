package kr.danta.paper.combat.live;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatProjectileAimTest {

    @Test
    void raisesAimPointForLongerShotsToCompensateArrowDrop() {
        CombatProjectileAim.AimOffset shortShot = CombatProjectileAim.compensatedOffset(
                4.0, 0.0, 1.6, 0.05);
        CombatProjectileAim.AimOffset longShot = CombatProjectileAim.compensatedOffset(
                14.0, 0.0, 1.6, 0.05);

        assertTrue(shortShot.vertical() > 0.0);
        assertTrue(longShot.vertical() > shortShot.vertical());
        assertEquals(0.0, shortShot.horizontal(), 1.0e-9);
        assertEquals(0.0, longShot.horizontal(), 1.0e-9);
    }

    @Test
    void preservesExistingVerticalDifferenceWhileAddingDropCompensation() {
        CombatProjectileAim.AimOffset offset = CombatProjectileAim.compensatedOffset(
                10.0, 2.0, 1.6, 0.05);

        assertTrue(offset.vertical() > 2.0);
    }
}
