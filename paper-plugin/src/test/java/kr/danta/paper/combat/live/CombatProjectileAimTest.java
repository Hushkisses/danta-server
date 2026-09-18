package kr.danta.paper.combat.live;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatProjectileAimTest {

    @Test
    void solvesFlatShotSoProjectileCrossesTargetHeightAtFourteenBlocks() {
        CombatProjectileAim.AimOffset aim = CombatProjectileAim.compensatedOffset(
                14.0, 0.0, 1.6, 0.05, 0.99);

        double angle = Math.atan2(aim.vertical(), 14.0);
        double height = CombatProjectileAim.simulatedHeightAtDistance(
                14.0, 1.6, angle, 0.05, 0.99);

        assertEquals(0.0, height, 0.08);
        assertTrue(aim.vertical() > 0.0);
        assertTrue(aim.vertical() < 2.8);
    }

    @Test
    void solvesElevatedTargetWithoutUsingFixedLinearLift() {
        CombatProjectileAim.AimOffset aim = CombatProjectileAim.compensatedOffset(
                10.0, 1.0, 1.6, 0.05, 0.99);

        double angle = Math.atan2(aim.vertical(), 10.0);
        double height = CombatProjectileAim.simulatedHeightAtDistance(
                10.0, 1.6, angle, 0.05, 0.99);

        assertEquals(1.0, height, 0.08);
    }
}
