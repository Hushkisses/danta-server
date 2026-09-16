package kr.danta.core.npc;
import kr.danta.core.state.GameState;import java.util.*;
/** DEV-093 registry for NPC-controlled nations. Does not invent strategic scoring or autonomous actions. */
public final class NpcNationService {
 private final GameState gameState; private final Map<String,NpcNationState> states=new LinkedHashMap<>();
 public NpcNationService(GameState gameState){this.gameState=Objects.requireNonNull(gameState);}
 public synchronized NpcNationState register(String nationId){requireNation(nationId);if(states.containsKey(nationId))throw new IllegalStateException("nation is already npc controlled");NpcNationState s=new NpcNationState(nationId);states.put(nationId,s);return s;}
 public synchronized void unregister(String nationId){if(states.remove(nationId)==null)throw new IllegalStateException("nation is not npc controlled");}
 public synchronized boolean isNpc(String nationId){return states.containsKey(nationId);} public synchronized Optional<NpcNationState> state(String id){return Optional.ofNullable(states.get(id));} public synchronized List<NpcNationState> states(){return List.copyOf(states.values());}
 private void requireNation(String id){if(!gameState.hasNation(id))throw new IllegalArgumentException("nation does not exist: "+id);}
}
