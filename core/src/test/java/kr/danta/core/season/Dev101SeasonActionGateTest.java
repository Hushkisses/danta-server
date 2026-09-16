package kr.danta.core.season;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class Dev101SeasonActionGateTest {
 private final SeasonActionGate g=new SeasonActionGate(()->0L); private static long h(long n){return n*3_600_000L;}
 @Test void playerWarUnlocksAtFourHours(){assertFalse(g.check(SeasonAction.PLAYER_WAR,h(4)-1).allowed());assertTrue(g.check(SeasonAction.PLAYER_WAR,h(4)).allowed());}
 @Test void majorSiegeRunsEightToBeforeFortyEight(){assertFalse(g.check(SeasonAction.MAJOR_POINT_SIEGE,h(8)-1).allowed());assertTrue(g.check(SeasonAction.MAJOR_POINT_SIEGE,h(8)).allowed());assertFalse(g.check(SeasonAction.MAJOR_POINT_SIEGE,h(48)).allowed());}
 @Test void capitalSiegeUnlocksAtTwenty(){assertFalse(g.check(SeasonAction.CAPITAL_SIEGE,h(20)-1).allowed());assertTrue(g.check(SeasonAction.CAPITAL_SIEGE,h(20)).allowed());}
 @Test void allNewActionsStopAtFifty(){for(var a:SeasonAction.values())assertFalse(g.check(a,h(50)).allowed());}
}