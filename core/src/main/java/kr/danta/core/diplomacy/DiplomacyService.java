package kr.danta.core.diplomacy;
import kr.danta.core.state.GameState;
import java.util.*;
/** DEV-090 authoritative symmetric relationship service. DEV-091 will own alliance auto-entry into wars. */
public final class DiplomacyService {
 private final GameState gameState; private final Map<String,DiplomaticRelation> relations=new LinkedHashMap<>();
 public DiplomacyService(GameState gameState){this.gameState=Objects.requireNonNull(gameState);}
 public synchronized DiplomaticStatus status(String a,String b){validatePair(a,b);return Optional.ofNullable(relations.get(key(a,b))).map(DiplomaticRelation::status).orElse(DiplomaticStatus.NEUTRAL);}
 public synchronized DiplomaticRelation setStatus(String a,String b,DiplomaticStatus status){validatePair(a,b);Objects.requireNonNull(status);String k=key(a,b);if(status==DiplomaticStatus.NEUTRAL){relations.remove(k);return new DiplomaticRelation(a,b,status);} DiplomaticRelation r=new DiplomaticRelation(a,b,status);relations.put(k,r);return r;}
 public synchronized List<DiplomaticRelation> relations(){return List.copyOf(relations.values());}
 public synchronized List<DiplomaticRelation> relationsOf(String nationId){requireNation(nationId);return relations.values().stream().filter(r->r.involves(nationId)).toList();}
 public synchronized void clear(){relations.clear();}
 public synchronized void restore(DiplomaticRelation relation){validatePair(relation.nationAId(),relation.nationBId());if(relation.status()!=DiplomaticStatus.NEUTRAL)relations.put(key(relation.nationAId(),relation.nationBId()),relation);}
 private void validatePair(String a,String b){requireNation(a);requireNation(b);if(a.equals(b))throw new IllegalArgumentException("self relation is not allowed");}
 private void requireNation(String id){if(!gameState.hasNation(id))throw new IllegalArgumentException("nation does not exist: "+id);}
 private static String key(String a,String b){return a.compareTo(b)<0?a+"\\0"+b:b+"\\0"+a;}
}
