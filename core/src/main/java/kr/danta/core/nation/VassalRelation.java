package kr.danta.core.nation;
import java.util.Objects;
/** DEV-095 authoritative player-nation subordination relation. Tribute/restrictions are DEV-096. */
public record VassalRelation(String vassalNationId,String overlordNationId,long vassalizedAtRuntimeMillis){public VassalRelation{vassalNationId=req(vassalNationId);overlordNationId=req(overlordNationId);if(vassalNationId.equals(overlordNationId))throw new IllegalArgumentException("vassal and overlord must differ");if(vassalizedAtRuntimeMillis<0)throw new IllegalArgumentException("negative vassalized runtime");}private static String req(String s){Objects.requireNonNull(s);s=s.trim();if(s.isEmpty())throw new IllegalArgumentException("blank nation id");return s;}}
