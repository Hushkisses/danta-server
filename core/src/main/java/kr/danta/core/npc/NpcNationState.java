package kr.danta.core.npc;
import java.util.Objects;
/** DEV-093 NPC-control metadata layered on an existing NationState. */
public final class NpcNationState {
 private final String nationId; private StrategicAiPhase phase=StrategicAiPhase.IDLE; private String decisionKey;
 public NpcNationState(String nationId){this.nationId=req(nationId);}
 public String nationId(){return nationId;} public synchronized StrategicAiPhase phase(){return phase;} public synchronized String decisionKey(){return decisionKey;}
 public synchronized void beginEvaluation(){require(StrategicAiPhase.IDLE);phase=StrategicAiPhase.EVALUATING;decisionKey=null;}
 public synchronized void decide(String key){require(StrategicAiPhase.EVALUATING);decisionKey=req(key);phase=StrategicAiPhase.DECIDED;}
 public synchronized void beginExecution(){require(StrategicAiPhase.DECIDED);phase=StrategicAiPhase.EXECUTING;}
 public synchronized void finishExecution(){require(StrategicAiPhase.EXECUTING);phase=StrategicAiPhase.IDLE;decisionKey=null;}
 public synchronized void cancel(){phase=StrategicAiPhase.IDLE;decisionKey=null;}
 private void require(StrategicAiPhase expected){if(phase!=expected)throw new IllegalStateException("npc strategic ai phase must be "+expected+" but was "+phase);}
 private static String req(String s){Objects.requireNonNull(s);s=s.trim();if(s.isEmpty())throw new IllegalArgumentException("npc value must not be blank");return s;}
}
