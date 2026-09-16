package kr.danta.core.nation;
import java.util.Objects;
public record IndependenceWarState(String vassalNationId,String overlordNationId,long declaredAtRuntimeMillis,long holdUntilRuntimeMillis,long redeclareAfterRuntimeMillis,boolean active){
 public IndependenceWarState{vassalNationId=req(vassalNationId);overlordNationId=req(overlordNationId);if(declaredAtRuntimeMillis<0||holdUntilRuntimeMillis<0||redeclareAfterRuntimeMillis<0)throw new IllegalArgumentException("negative independence runtime");}
 private static String req(String s){Objects.requireNonNull(s);s=s.trim();if(s.isEmpty())throw new IllegalArgumentException("blank nation id");return s;}
}
