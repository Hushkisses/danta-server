package kr.danta.core.season;
public enum SeasonPhase {
 OPENING("초반",0,8), EXPANSION("확장기",8,20), CONFLICT("쟁패기",20,38), ENDGAME("최종전",38,50), FINISHED("시즌 종료",50,Long.MAX_VALUE);
 private static final long HOUR_MS=3_600_000L; private final String name; private final long startHour,endHour;
 SeasonPhase(String name,long startHour,long endHour){this.name=name;this.startHour=startHour;this.endHour=endHour;}
 public String displayName(){return name;} public long startRuntimeMillis(){return startHour*HOUR_MS;}
 public long endRuntimeMillis(){return endHour==Long.MAX_VALUE?Long.MAX_VALUE:endHour*HOUR_MS;}
 public static SeasonPhase atRuntimeMillis(long ms){if(ms<0)throw new IllegalArgumentException("runtimeMillis must be >= 0");for(var p:values())if(ms>=p.startRuntimeMillis()&&ms<p.endRuntimeMillis())return p;return FINISHED;}
}