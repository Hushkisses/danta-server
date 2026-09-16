package kr.danta.core.diplomacy;
import java.util.*;
/** DEV-091 explicit war sides. Support participation is nation-level; no individual mercenary participation. */
public final class War {
 private final UUID warId; private final String initiatorNationId; private final String targetNationId; private final LinkedHashSet<String> attackers=new LinkedHashSet<>(); private final LinkedHashSet<String> defenders=new LinkedHashSet<>();
 public War(UUID id,String initiator,String target){warId=Objects.requireNonNull(id);initiatorNationId=req(initiator);targetNationId=req(target);if(initiatorNationId.equals(targetNationId))throw new IllegalArgumentException("same nation war");attackers.add(initiatorNationId);defenders.add(targetNationId);}
 public UUID warId(){return warId;} public String initiatorNationId(){return initiatorNationId;} public String targetNationId(){return targetNationId;} public Set<String> attackers(){return Set.copyOf(attackers);} public Set<String> defenders(){return Set.copyOf(defenders);}
 public boolean participates(String id){return attackers.contains(id)||defenders.contains(id);} public WarSide sideOf(String id){if(attackers.contains(id))return WarSide.ATTACKER;if(defenders.contains(id))return WarSide.DEFENDER;return null;}
 void join(String id,WarSide side){if(participates(id))throw new IllegalStateException("nation already participates");(side==WarSide.ATTACKER?attackers:defenders).add(req(id));}
 private static String req(String s){Objects.requireNonNull(s);s=s.trim();if(s.isEmpty())throw new IllegalArgumentException("nation id blank");return s;}
}
