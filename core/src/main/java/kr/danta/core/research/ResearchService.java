package kr.danta.core.research;

import kr.danta.core.runtime.RuntimeScheduledTask;
import kr.danta.core.runtime.RuntimeScheduler;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** DEV-084 runtime research queue. Costs/prerequisites are deliberately enforced by later tickets. */
public final class ResearchService {
    public static final String TASK_TYPE = "research.complete";
    private final RuntimeScheduler scheduler;
    private final Map<String, ResearchDefinition> definitions;
    private final Map<String, ResearchState> states = new LinkedHashMap<>();

    public ResearchService(RuntimeScheduler scheduler, Map<String, ResearchDefinition> definitions) {
        this.scheduler = Objects.requireNonNull(scheduler);
        this.definitions = Map.copyOf(Objects.requireNonNull(definitions));
    }
    public synchronized ResearchState state(String nationId) { return states.computeIfAbsent(nationId, ResearchState::new); }
    public synchronized ResearchQueueEntry reserve(String nationId, String researchId) {
        ResearchDefinition definition = definition(researchId); ResearchState state=state(nationId);
        if(state.completed().contains(definition.researchId())) throw new IllegalStateException("research already completed: "+researchId);
        if(state.containsQueued(definition.researchId())) throw new IllegalStateException("research already queued: "+researchId);
        ResearchQueueEntry entry=new ResearchQueueEntry(UUID.randomUUID(),definition.researchId(),null,null); state.addQueue(entry); startAvailable(state); return findEntry(state,entry.entryId());
    }
    public synchronized ResearchQueueEntry cancel(String nationId, UUID entryId) { ResearchState state=state(nationId); ResearchQueueEntry entry=state.remove(entryId); if(entry.active()) scheduler.cancel(entry.taskId()); startAvailable(state); return entry; }
    public synchronized void complete(UUID entryId, String nationId) { ResearchState state=state(nationId); ResearchQueueEntry entry=state.remove(entryId); state.complete(entry.researchId()); startAvailable(state); }
    public synchronized List<ResearchQueueEntry> queue(String nationId) { return state(nationId).queue(); }
    public synchronized void setResearchSlots(String nationId,int slots) { ResearchState state=state(nationId); state.setResearchSlots(slots); startAvailable(state); }
    public synchronized void restore(ResearchState restored) { if(states.putIfAbsent(restored.nationId(),restored)!=null) throw new IllegalArgumentException("duplicate research state"); for(ResearchQueueEntry e:restored.queue()) if(e.active()) scheduler.restore(new RuntimeScheduledTask(e.taskId(),e.dueRuntimeMillis(),TASK_TYPE,payload(restored.nationId(),e.entryId()))); startAvailable(restored); }
    private void startAvailable(ResearchState state) { long active=state.queue().stream().filter(ResearchQueueEntry::active).count(); if(active>=state.researchSlots()) return; for(ResearchQueueEntry e:state.queue()) { if(active>=state.researchSlots()) break; if(e.active()) continue; ResearchDefinition d=definition(e.researchId()); RuntimeScheduledTask task=scheduler.scheduleAfter(Duration.ofMillis(d.durationRuntimeMillis()),TASK_TYPE,payload(state.nationId(),e.entryId())); state.replace(e,e.activate(task.id(),task.dueRuntimeMillis())); active++; } }
    private ResearchQueueEntry findEntry(ResearchState s,UUID id){return s.queue().stream().filter(e->e.entryId().equals(id)).findFirst().orElseThrow();}
    private ResearchDefinition definition(String id){ResearchDefinition d=definitions.get(id.trim().toLowerCase()); if(d==null) throw new IllegalArgumentException("unknown research: "+id); return d;}
    public static Map<String,String> payload(String nationId,UUID entryId){return Map.of("nationId",nationId,"entryId",entryId.toString());}
}
