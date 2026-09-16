package kr.danta.core.diplomacy;
import kr.danta.core.state.GameState;import kr.danta.core.territory.StrategicPoint;import java.util.*;
/** DEV-092 derives passage/supply rights from authoritative diplomatic status. No extra persisted state. */
public final class DiplomaticAccessService {
 private final GameState gameState; private final DiplomacyService diplomacy;
 public DiplomaticAccessService(GameState g,DiplomacyService d){gameState=Objects.requireNonNull(g);diplomacy=Objects.requireNonNull(d);}
 public boolean hasRight(String guest,String host,AccessRight right){requireNation(guest);requireNation(host);Objects.requireNonNull(right);if(guest.equals(host))return true;DiplomaticStatus s=diplomacy.status(guest,host);return switch(s){case FRIENDLY,ALLIANCE->true;case NEUTRAL,WAR->false;};}
 public boolean canEnterPoint(String nation,String pointId){StrategicPoint p=gameState.strategicPoint(pointId).orElseThrow(()->new IllegalArgumentException("strategic point not found: "+pointId));String owner=p.ownerNationId().orElse(null);return owner==null||owner.equals(nation)||hasRight(nation,owner,AccessRight.PASSAGE);}
 public boolean canUsePointForSupply(String nation,String pointId){StrategicPoint p=gameState.strategicPoint(pointId).orElseThrow(()->new IllegalArgumentException("strategic point not found: "+pointId));String owner=p.ownerNationId().orElse(null);return owner!=null&&(owner.equals(nation)||hasRight(nation,owner,AccessRight.SUPPLY));}
 private void requireNation(String id){if(!gameState.hasNation(id))throw new IllegalArgumentException("nation does not exist: "+id);}
}
