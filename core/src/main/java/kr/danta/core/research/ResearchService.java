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
    public enum Eligibility { ELIGIBLE, UNKNOWN_RESEARCH, ALREADY_COMPLETED, ALREADY_QUEUED, MISSING_PREREQUISITES }
    public static final String TASK_TYPE = "research.complete";
    private final RuntimeScheduler scheduler;
    private final Map<String, ResearchDefinition> definitions;
    private final Map<String, ResearchState> states = new LinkedHashMap<>();

    public ResearchService(RuntimeScheduler scheduler, Map<String, ResearchDefinition> definitions) {
        this.scheduler = Objects.requireNonNull(scheduler);
        this.definitions = Map.copyOf(Objects.requireNonNull(definitions));
    }
    public synchronized List<ResearchState> states() { return List.copyOf(states.values()); }
    public synchronized ResearchState state(String nationId) { return states.computeIfAbsent(nationId, ResearchState::new); }
    public synchronized Eligibility eligibility(String nationId, String researchId) {
        ResearchDefinition definition = definitions.get(researchId.trim().toLowerCase());
        if (definition == null) return Eligibility.UNKNOWN_RESEARCH;
        ResearchState state = state(nationId);
        if (state.completed().contains(definition.researchId())) return Eligibility.ALREADY_COMPLETED;
        if (state.containsQueued(definition.researchId())) return Eligibility.ALREADY_QUEUED;
        if (!state.completed().containsAll(definition.prerequisites())) return Eligibility.MISSING_PREREQUISITES;
        return Eligibility.ELIGIBLE;
    }
    public synchronized List<String> missingPrerequisites(String nationId, String researchId) {
        ResearchDefinition definition = definition(researchId);
        var completed = state(nationId).completed();
        return definition.prerequisites().stream().filter(id -> !completed.contains(id)).toList();
    }
    public synchronized List<ResearchDefinition> definitions(ResearchField field) {
        return definitions.values().stream().filter(d -> d.field() == field)
                .sorted(java.util.Comparator.comparing(ResearchDefinition::tier).thenComparing(ResearchDefinition::researchId)).toList();
    }
    public synchronized ResearchQueueEntry reserve(String nationId, String researchId) {
        ResearchDefinition definition = definition(researchId); ResearchState state=state(nationId);
        Eligibility eligibility = eligibility(nationId, researchId);
        if (eligibility == Eligibility.ALREADY_COMPLETED) throw new IllegalStateException("research already completed: "+researchId);
        if (eligibility == Eligibility.ALREADY_QUEUED) throw new IllegalStateException("research already queued: "+researchId);
        if (eligibility == Eligibility.MISSING_PREREQUISITES) throw new IllegalStateException("missing research prerequisites: " + String.join(",", missingPrerequisites(nationId, researchId)));
        ResearchQueueEntry entry=new ResearchQueueEntry(UUID.randomUUID(),definition.researchId(),null,null); state.addQueue(entry); startAvailable(state); return findEntry(state,entry.entryId());
    }
    public synchronized ResearchQueueEntry cancel(String nationId, UUID entryId) { ResearchState state=state(nationId); ResearchQueueEntry entry=state.remove(entryId); if(entry.active()) scheduler.cancel(entry.taskId()); startAvailable(state); return entry; }
    public synchronized void complete(UUID entryId, String nationId) { ResearchState state=state(nationId); ResearchQueueEntry entry=state.remove(entryId); state.complete(entry.researchId()); startAvailable(state); }
    public synchronized List<ResearchQueueEntry> queue(String nationId) { return state(nationId).queue(); }
    public synchronized void setResearchSlots(String nationId,int slots) { ResearchState state=state(nationId); state.setResearchSlots(slots); startAvailable(state); }
    public synchronized void setDoctrineSlots(String nationId, int slots) { state(nationId).setDoctrineSlots(slots); }
    public synchronized DoctrineSelection selectDoctrine(String nationId, String researchId) {
        ResearchDefinition definition = definition(researchId);
        if (definition.tier() != ResearchTier.TIER_4 || definition.doctrineKey() == null)
            throw new IllegalArgumentException("research is not a doctrine: " + researchId);
        ResearchState state = state(nationId);
        if (!state.completed().contains(definition.researchId()))
            throw new IllegalStateException("doctrine research not completed: " + researchId);
        DoctrineSelection selection = new DoctrineSelection(definition.doctrineKey(), definition.field());
        state.selectDoctrine(selection);
        return selection;
    }
    public synchronized DoctrineSelection removeDoctrine(String nationId, String doctrineKey) {
        return state(nationId).removeDoctrine(doctrineKey.trim().toLowerCase());
    }
    public synchronized void restore(ResearchState restored) { if(states.putIfAbsent(restored.nationId(),restored)!=null) throw new IllegalArgumentException("duplicate research state"); for(ResearchQueueEntry e:restored.queue()) if(e.active()) scheduler.restore(new RuntimeScheduledTask(e.taskId(),e.dueRuntimeMillis(),TASK_TYPE,payload(restored.nationId(),e.entryId()))); startAvailable(restored); }
    private void startAvailable(ResearchState state) { long active=state.queue().stream().filter(ResearchQueueEntry::active).count(); if(active>=state.researchSlots()) return; for(ResearchQueueEntry e:state.queue()) { if(active>=state.researchSlots()) break; if(e.active()) continue; ResearchDefinition d=definition(e.researchId()); RuntimeScheduledTask task=scheduler.scheduleAfter(Duration.ofMillis(d.durationRuntimeMillis()),TASK_TYPE,payload(state.nationId(),e.entryId())); state.replace(e,e.activate(task.id(),task.dueRuntimeMillis())); active++; } }
    private ResearchQueueEntry findEntry(ResearchState s,UUID id){return s.queue().stream().filter(e->e.entryId().equals(id)).findFirst().orElseThrow();}
    private ResearchDefinition definition(String id){ResearchDefinition d=definitions.get(id.trim().toLowerCase()); if(d==null) throw new IllegalArgumentException("unknown research: "+id); return d;}
    public static Map<String,String> payload(String nationId,UUID entryId){return Map.of("nationId",nationId,"entryId",entryId.toString());}
}
