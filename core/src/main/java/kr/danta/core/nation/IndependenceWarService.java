package kr.danta.core.nation;
import kr.danta.core.state.GameState;
import java.time.Duration;import java.util.*;
/** DEV-097 independence war. All durations are server-runtime based and provisional/configurable. */
public final class IndependenceWarService {
 public static final Duration DEFAULT_PROVISIONAL_MIN_SUBORDINATION=Duration.ofHours(1);
 public static final Duration DEFAULT_PROVISIONAL_CAPITAL_HOLD=Duration.ofMinutes(10);
 public static final Duration DEFAULT_PROVISIONAL_REDECLARE_COOLDOWN=Duration.ofMinutes(30);
 public enum Outcome{ONGOING,INDEPENDENCE_SUCCESS,FAILED_CAPITAL_LOST}
 private final GameState gameState;private final VassalService vassals;private final long minSubordinationMs,holdMs,cooldownMs;
 private final Map<String,IndependenceWarState> states=new LinkedHashMap<>();
 public IndependenceWarService(GameState g,VassalService v){this(g,v,DEFAULT_PROVISIONAL_MIN_SUBORDINATION,DEFAULT_PROVISIONAL_CAPITAL_HOLD,DEFAULT_PROVISIONAL_REDECLARE_COOLDOWN);}
 public IndependenceWarService(GameState g,VassalService v,Duration min,Duration hold,Duration cooldown){gameState=Objects.requireNonNull(g);vassals=Objects.requireNonNull(v);minSubordinationMs=positive(min);holdMs=positive(hold);cooldownMs=positive(cooldown);}
 public synchronized IndependenceWarState declare(String nationId,long now){VassalRelation r=vassals.relation(nationId).orElseThrow(()->new IllegalStateException("nation is not vassal"));if(now-r.vassalizedAtRuntimeMillis()<minSubordinationMs)throw new IllegalStateException("minimum subordination period not elapsed");IndependenceWarState prev=states.get(nationId);if(prev!=null&&prev.active())throw new IllegalStateException("independence war already active");if(prev!=null&&now<prev.redeclareAfterRuntimeMillis())throw new IllegalStateException("independence war redeclare cooldown active");if(!controlsOwnCapital(nationId))throw new IllegalStateException("vassal does not control own capital");IndependenceWarState s=new IndependenceWarState(nationId,r.overlordNationId(),now,Math.addExact(now,holdMs),0,true);states.put(nationId,s);return s;}
 public synchronized Outcome tick(String nationId,long now){IndependenceWarState s=states.get(nationId);if(s==null||!s.active())return Outcome.ONGOING;if(!controlsOwnCapital(nationId)){states.put(nationId,new IndependenceWarState(s.vassalNationId(),s.overlordNationId(),s.declaredAtRuntimeMillis(),s.holdUntilRuntimeMillis(),Math.addExact(now,cooldownMs),false));return Outcome.FAILED_CAPITAL_LOST;}if(now>=s.holdUntilRuntimeMillis()){vassals.release(nationId);states.remove(nationId);return Outcome.INDEPENDENCE_SUCCESS;}return Outcome.ONGOING;}
 public synchronized Optional<IndependenceWarState> state(String id){return Optional.ofNullable(states.get(id));}
 public synchronized List<IndependenceWarState> states(){return List.copyOf(states.values());}
 public synchronized void restore(IndependenceWarState s){states.put(s.vassalNationId(),s);}
 public synchronized void clear(){states.clear();}
 public long minimumSubordinationMillis(){return minSubordinationMs;}public long capitalHoldMillis(){return holdMs;}public long redeclareCooldownMillis(){return cooldownMs;}
 private boolean controlsOwnCapital(String id){NationState n=gameState.nation(id).orElseThrow(()->new IllegalArgumentException("nation does not exist: "+id));String cap=n.capitalPointId().orElseThrow(()->new IllegalStateException("nation has no designated capital"));return gameState.strategicPoint(cap).flatMap(p->p.ownerNationId()).map(id::equals).orElse(false);}
 private static long positive(Duration d){Objects.requireNonNull(d);long m=d.toMillis();if(m<=0)throw new IllegalArgumentException("duration must be positive");return m;}
}
