package kr.danta.core.chronicle;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Dev107ChronicleServiceTest {
    @Test void recordsAndReturnsRecentEntriesInChronologicalOrder() {
        ChronicleService service = new ChronicleService();
        service.record(100L, ChronicleEventType.WAR_DECLARED, "적국이 청국에 전쟁을 선포");
        service.record(200L, ChronicleEventType.TERRITORY_CHANGE, "농장 거점 점령");
        service.record(300L, ChronicleEventType.VASSALIZED, "청국이 속국화");

        assertEquals(3, service.entries().size());
        assertEquals(2, service.recent(2).size());
        assertEquals(200L, service.recent(2).get(0).runtimeMillis());
        assertEquals(300L, service.recent(2).get(1).runtimeMillis());
        assertTrue(service.recent(0).isEmpty());
    }
}
