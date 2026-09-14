package kr.danta.core.runtime;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuntimeSchedulerTest {
    @Test
    void executesOnlyWhenRuntimeDeadlineIsReached() {
        AtomicLong nanos = new AtomicLong();
        RuntimeClockService clock = new RuntimeClockService(nanos::get);
        clock.start();
        RuntimeScheduler scheduler = new RuntimeScheduler(clock);
        List<String> executed = new ArrayList<>();
        scheduler.registerHandler("test", task -> executed.add(task.payload().get("name")));

        scheduler.scheduleAfter(Duration.ofSeconds(10), "test", Map.of("name", "A"));
        nanos.set(Duration.ofSeconds(9).toNanos());
        assertTrue(scheduler.executeDueTasks().isEmpty());
        assertTrue(executed.isEmpty());

        nanos.set(Duration.ofSeconds(10).toNanos());
        assertEquals(1, scheduler.executeDueTasks().size());
        assertEquals(List.of("A"), executed);
        assertEquals(0, scheduler.size());
    }

    @Test
    void pausedClockDoesNotMakeTaskDue() {
        AtomicLong nanos = new AtomicLong();
        RuntimeClockService clock = new RuntimeClockService(nanos::get);
        clock.start();
        RuntimeScheduler scheduler = new RuntimeScheduler(clock);
        scheduler.registerHandler("test", task -> { });
        scheduler.scheduleAfter(Duration.ofSeconds(5), "test", Map.of());

        nanos.set(Duration.ofSeconds(2).toNanos());
        clock.pause();
        nanos.set(Duration.ofHours(1).toNanos());
        assertTrue(scheduler.executeDueTasks().isEmpty());

        clock.resume();
        nanos.set(Duration.ofHours(1).plusSeconds(3).toNanos());
        assertEquals(1, scheduler.executeDueTasks().size());
    }

    @Test
    void cancelledTaskNeverExecutes() {
        AtomicLong nanos = new AtomicLong();
        RuntimeClockService clock = new RuntimeClockService(nanos::get);
        clock.start();
        RuntimeScheduler scheduler = new RuntimeScheduler(clock);
        scheduler.registerHandler("test", task -> { throw new AssertionError("must not run"); });
        RuntimeScheduledTask task = scheduler.scheduleAfter(Duration.ofSeconds(1), "test", Map.of());

        assertTrue(scheduler.cancel(task.id()));
        assertFalse(scheduler.cancel(task.id()));
        nanos.set(Duration.ofSeconds(2).toNanos());
        assertTrue(scheduler.executeDueTasks().isEmpty());
    }
}
