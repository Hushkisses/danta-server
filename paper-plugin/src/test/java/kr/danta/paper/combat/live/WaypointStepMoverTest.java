package kr.danta.paper.combat.live;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WaypointStepMoverTest {

    @Test
    void advancesTowardGoalWithoutOvershooting() {
        WaypointStepMover mover = new WaypointStepMover();

        WaypointStepMover.Step step = mover.step(0.0, 64.0, 0.0, 10.0, 64.0, 0.0, 0.25);
        assertEquals(0.25, step.x(), 1.0e-9);
        assertEquals(64.0, step.y(), 1.0e-9);
        assertEquals(0.0, step.z(), 1.0e-9);

        WaypointStepMover.Step finalStep = mover.step(9.9, 64.0, 0.0, 10.0, 64.0, 0.0, 0.25);
        assertEquals(10.0, finalStep.x(), 1.0e-9);
        assertEquals(64.0, finalStep.y(), 1.0e-9);
        assertEquals(0.0, finalStep.z(), 1.0e-9);
    }
}
