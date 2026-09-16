package kr.danta.core.season;
import org.junit.jupiter.api.Test;import java.util.concurrent.atomic.AtomicLong;import static org.junit.jupiter.api.Assertions.*;
class Dev100SeasonPhaseServiceTest {
 @Test void boundariesFollowExecutionPlan(){assertEquals(SeasonPhase.OPENING,SeasonPhase.atRuntimeMillis(0));assertEquals(SeasonPhase.OPENING,SeasonPhase.atRuntimeMillis(h(8)-1));assertEquals(SeasonPhase.EXPANSION,SeasonPhase.atRuntimeMillis(h(8)));assertEquals(SeasonPhase.CONFLICT,SeasonPhase.atRuntimeMillis(h(20)));assertEquals(SeasonPhase.ENDGAME,SeasonPhase.atRuntimeMillis(h(38)));assertEquals(SeasonPhase.FINISHED,SeasonPhase.atRuntimeMillis(h(50)));}
 @Test void projectsExistingRuntime(){AtomicLong r=new AtomicLong(h(19));var s=new SeasonPhaseService(r::get);assertEquals(SeasonPhase.EXPANSION,s.current());r.set(h(38));assertEquals(SeasonPhase.ENDGAME,s.current());}
 private static long h(long x){return x*3_600_000L;}
}