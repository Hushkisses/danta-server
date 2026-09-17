package kr.danta.paper.combat.live;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatProjectileAimTest {

    @Test
    void raisesAimPointForLongerShotsToCompensateArrowDrop() {
        CombatProjectileAim.AimOffset shortShot = CombatProjectileAim.compensatedOffset(
                4.0, 0.0, 0.20);
        CombatProjectileAim.AimOffset longShot = CombatProjectileAim.compensatedOffset(
                14.0, 0.0, 0.20);

        assertEquals(0.8, shortShot.vertical(), 1.0e-9);
        assertEquals(2.8, longShot.vertical(), 1.0e-9);
        assertTrue(longShot.vertical() > shortShot.vertical());
    }

    @Test
    void preservesExistingVerticalDifferenceWhileAddingTrajectoryLift() {
        CombatProjectileAim.AimOffset offset = CombatProjectileAim.compensatedOffset(
                10.0, 2.0, 0.20);

        assertEquals(4.0, offset.vertical(), 1.0e-9);
    }
}
