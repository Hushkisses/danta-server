package kr.danta.core.npc;
import kr.danta.core.diplomacy.*;import kr.danta.core.state.GameState;import kr.danta.core.territory.TerritoryService;import java.util.*;
/** DEV-094 authoritative NPC political transitions. Numeric influence/thresholds remain deliberately deferred. */
public final class NpcPoliticalService {
 private final GameState gameState;private final NpcNationService npcs;private final DiplomacyService diplomacy;private final TerritoryService territory;
 public NpcPoliticalService(GameState g,NpcNationService n,DiplomacyService d,TerritoryService t){gameState=Objects.requireNonNull(g);npcs=Objects.requireNonNull(n);diplomacy=Objects.requireNonNull(d);territory=Objects.requireNonNull(t);}
 public synchronized NpcNationState ally(String npcId,String playerNation){NpcNationState npc=requireNpc(npcId);requireOtherNation(npcId,playerNation);npc.allyWith(playerNation);diplomacy.setStatus(npcId,playerNation,DiplomaticStatus.FRIENDLY);return npc;}
 public synchronized NpcNationState subjugate(String npcId,String overlord){NpcNationState npc=requireNpc(npcId);requireOtherNation(npcId,overlord);npc.subjugateBy(overlord);diplomacy.setStatus(npcId,overlord,DiplomaticStatus.FRIENDLY);return npc;}
 public synchronized AnnexationResult annex(String npcId,String annexer){NpcNationState npc=requireNpc(npcId);requireOtherNation(npcId,annexer);npc.annexBy(annexer);int transferred=0;for(var p:gameState.strategicPoints())if(p.ownerNationId().filter(npcId::equals).isPresent()){if(territory.changeOwner(p.pointId(),annexer,"npc-annexation:"+npcId).changed())transferred++;}diplomacy.relationsOf(npcId).forEach(r->diplomacy.setStatus(r.nationAId(),r.nationBId(),DiplomaticStatus.NEUTRAL));return new AnnexationResult(npc,transferred);}
 private NpcNationState requireNpc(String id){return npcs.state(id).orElseThrow(()->new IllegalStateException("nation is not npc controlled"));}private void requireOtherNation(String npc,String other){if(!gameState.hasNation(other))throw new IllegalArgumentException("nation does not exist: "+other);if(npc.equals(other))throw new IllegalArgumentException("npc cannot target itself");}
 public record AnnexationResult(NpcNationState npc,int transferredPoints){}
}
