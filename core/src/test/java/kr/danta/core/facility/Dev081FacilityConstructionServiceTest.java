package kr.danta.core.facility;

import kr.danta.core.runtime.*;
import kr.danta.core.state.GameState;
import kr.danta.core.territory.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class Dev081FacilityConstructionServiceTest {
    @Test void buildCompletesOnlyAfterServerRuntimeDeadline() {
        RuntimeClockService clock = new RuntimeClockService();
        clock.start();
        RuntimeScheduler scheduler = new RuntimeScheduler(clock);
        GameState state = stateWithPoint(1);
        FacilityService facilities = new FacilityService(state);
        FacilityConstructionService construction = new FacilityConstructionService(state, facilities, scheduler);
        scheduler.registerHandler(FacilityConstructionService.TASK_TYPE,
                task -> construction.complete(UUID.fromString(task.payload().get("constructionId"))));

        construction.scheduleBuild("p1", "warehouse", Duration.ofSeconds(10));
        clock.setElapsedMillis(Duration.ofSeconds(9).toMillis());
        scheduler.executeDueTasks();
        assertTrue(facilities.facilities("p1").isEmpty());

        clock.setElapsedMillis(Duration.ofSeconds(10).toMillis());
        assertTrue(scheduler.executeDueTasks().getFirst().success());
        assertEquals(FacilityTier.I, facilities.facility("p1", "warehouse").orElseThrow().tier());
        assertTrue(construction.pending().isEmpty());
    }

    @Test void pausedRuntimeDoesNotAdvanceConstruction() {
        RuntimeClockService clock = new RuntimeClockService();
        clock.start();
        RuntimeScheduler scheduler = new RuntimeScheduler(clock);
        GameState state = stateWithPoint(1);
        FacilityService facilities = new FacilityService(state);
        FacilityConstructionService construction = new FacilityConstructionService(state, facilities, scheduler);
        scheduler.registerHandler(FacilityConstructionService.TASK_TYPE,
                task -> construction.complete(UUID.fromString(task.payload().get("constructionId"))));

        construction.scheduleBuild("p1", "warehouse", Duration.ofSeconds(5));
        clock.setElapsedMillis(Duration.ofSeconds(2).toMillis());
        clock.pause();
        // Paused runtime must remain frozen even while real wall-clock time passes.
        // setElapsedMillis() is a restore/dev mutator, not elapsed wall-clock time, so
        // using it here would intentionally move the authoritative runtime forward.
        try {
            Thread.sleep(20L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("test interrupted", e);
        }
        long pausedAt = clock.elapsedMillis();
        assertTrue(pausedAt >= Duration.ofSeconds(2).toMillis());
        try {
            Thread.sleep(20L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("test interrupted", e);
        }
        assertEquals(pausedAt, clock.elapsedMillis());
        assertTrue(scheduler.executeDueTasks().isEmpty());
        assertTrue(facilities.facilities("p1").isEmpty());
    }

    @Test void pendingBuildReservesSlotAndRestoreKeepsOriginalDeadline() {
        RuntimeClockService clock = new RuntimeClockService();
        clock.start();
        RuntimeScheduler scheduler = new RuntimeScheduler(clock);
        GameState state = stateWithPoint(1);
        FacilityService facilities = new FacilityService(state);
        FacilityConstructionService construction = new FacilityConstructionService(state, facilities, scheduler);

        FacilityConstructionState first = construction.scheduleBuild("p1", "warehouse", Duration.ofSeconds(10));
        assertThrows(IllegalStateException.class, () -> construction.scheduleBuild("p1", "tax", Duration.ofSeconds(10)));

        RuntimeScheduler restoredScheduler = new RuntimeScheduler(clock);
        FacilityConstructionService restored = new FacilityConstructionService(state, facilities, restoredScheduler);
        restored.restore(first);
        assertEquals(first.dueRuntimeMillis(), restoredScheduler.nextTask().orElseThrow().dueRuntimeMillis());
    }

    private static GameState stateWithPoint(int slots) {
        GameState state = new GameState();
        state.addStrategicPoint(new StrategicPoint("p1", "P1", StrategicPointType.FARM,
                new PointPosition("world", 0, 64, 0), slots));
        return state;
    }
}
