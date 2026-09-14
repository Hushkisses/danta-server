package kr.danta.core.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class RuntimeClockServiceTest {
    @Test
    void advancesOnlyWhileRunning() {
        AtomicLong now = new AtomicLong();
        RuntimeClockService clock = new RuntimeClockService(now::get);

        assertFalse(clock.isRunning());
        assertEquals(Duration.ZERO, clock.elapsed());

        clock.start();
        now.addAndGet(Duration.ofSeconds(12).toNanos());
        assertTrue(clock.isRunning());
        assertEquals(Duration.ofSeconds(12), clock.elapsed());

        clock.stop();
        now.addAndGet(Duration.ofMinutes(5).toNanos());
        assertEquals(Duration.ofSeconds(12), clock.elapsed());

        clock.start();
        now.addAndGet(Duration.ofSeconds(8).toNanos());
        assertEquals(Duration.ofSeconds(20), clock.elapsed());
    }

    @Test
    void startAndStopAreIdempotent() {
        AtomicLong now = new AtomicLong();
        RuntimeClockService clock = new RuntimeClockService(now::get);

        clock.start();
        now.addAndGet(Duration.ofSeconds(3).toNanos());
        clock.start();
        now.addAndGet(Duration.ofSeconds(2).toNanos());
        clock.stop();
        clock.stop();

        assertEquals(Duration.ofSeconds(5), clock.elapsed());
    }
}
