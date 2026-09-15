package kr.danta.core.research;

import kr.danta.core.runtime.RuntimeClockService;
import kr.danta.core.runtime.RuntimeScheduler;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class Dev084ResearchServiceTest {
    private static ResearchDefinition def(String id,long ms){return new ResearchDefinition(id,id,ResearchField.MILITARY,ResearchTier.TIER_1,ms,0,Map.of(),List.of(),null,null);}
    @Test void oneSlotStartsFirstAndReservesSecond(){var scheduler=new RuntimeScheduler(new RuntimeClockService());var service=new ResearchService(scheduler,Map.of("a",def("a",1000),"b",def("b",1000)));var a=service.reserve("red","a");var b=service.reserve("red","b");assertTrue(a.active());assertFalse(b.active());assertEquals(1,scheduler.size());}
    @Test void secondSlotStartsReservedResearch(){var scheduler=new RuntimeScheduler(new RuntimeClockService());var service=new ResearchService(scheduler,Map.of("a",def("a",1000),"b",def("b",1000)));service.reserve("red","a");service.reserve("red","b");service.setResearchSlots("red",2);assertEquals(2,service.queue("red").stream().filter(ResearchQueueEntry::active).count());assertEquals(2,scheduler.size());}
    @Test void cancellationFreesSlotAndStartsNext(){var scheduler=new RuntimeScheduler(new RuntimeClockService());var service=new ResearchService(scheduler,Map.of("a",def("a",1000),"b",def("b",1000)));var a=service.reserve("red","a");service.reserve("red","b");service.cancel("red",a.entryId());assertEquals("b",service.queue("red").getFirst().researchId());assertTrue(service.queue("red").getFirst().active());assertEquals(1,scheduler.size());}
    @Test void duplicateReservationRejected(){var service=new ResearchService(new RuntimeScheduler(new RuntimeClockService()),Map.of("a",def("a",1000)));service.reserve("red","a");assertThrows(IllegalStateException.class,()->service.reserve("red","a"));}
}
