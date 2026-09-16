package kr.danta.core.season;
import java.util.Objects;import java.util.function.LongSupplier;
/** DEV-101 v0.3 season unlock rules. Existing battles are not cancelled by this gate. */
public final class SeasonActionGate {
 private static final long HOUR=3_600_000L;
 private final LongSupplier runtime;
 public SeasonActionGate(LongSupplier runtime){this.runtime=Objects.requireNonNull(runtime);}
 public Decision check(SeasonAction action){return check(action,runtime.getAsLong());}
 public Decision check(SeasonAction action,long ms){
  if(ms<0)throw new IllegalArgumentException("runtimeMillis must be >= 0");
  if(ms>=50*HOUR)return new Decision(false,"season-ended");
  return switch(action){
   case PLAYER_WAR -> new Decision(ms>=4*HOUR,ms<4*HOUR?"player-war-locked":"allowed");
   case MAJOR_POINT_SIEGE -> new Decision(ms>=8*HOUR&&ms<48*HOUR,ms<8*HOUR?"major-siege-locked":ms>=48*HOUR?"new-major-battle-locked":"allowed");
   case CAPITAL_SIEGE -> new Decision(ms>=20*HOUR,ms<20*HOUR?"capital-siege-locked":"allowed");
   case NEW_MAJOR_POINT_BATTLE -> new Decision(ms>=8*HOUR&&ms<48*HOUR,ms<8*HOUR?"major-siege-locked":ms>=48*HOUR?"new-major-battle-locked":"allowed");
  };
 }
 public record Decision(boolean allowed,String reason){}
}