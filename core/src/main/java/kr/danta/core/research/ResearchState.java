package kr.danta.core.research;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Authoritative per-nation research completion and reservation state. */
public final class ResearchState {
    private final String nationId;
    private final Set<String> completed = new LinkedHashSet<>();
    private final List<ResearchQueueEntry> queue = new ArrayList<>();
    private int researchSlots = 1;
    private int doctrineSlots = 2;
    private final List<DoctrineSelection> doctrines = new ArrayList<>();

    public ResearchState(String nationId) { this.nationId = requireId(nationId); }
    public static ResearchState restored(String nationId, int slots, Set<String> completed, List<ResearchQueueEntry> queue) {
        ResearchState state = new ResearchState(nationId);
        state.setResearchSlots(slots);
        if (completed != null) state.completed.addAll(completed.stream().map(String::toLowerCase).toList());
        if (queue != null) state.queue.addAll(queue);
        return state;
    }
    public static ResearchState restored(String nationId, int slots, Set<String> completed, List<ResearchQueueEntry> queue, int doctrineSlots, List<DoctrineSelection> doctrines) {
        ResearchState state = restored(nationId, slots, completed, queue);
        state.setDoctrineSlots(doctrineSlots);
        if (doctrines != null) state.doctrines.addAll(doctrines);
        if (state.doctrines.size() > state.doctrineSlots) throw new IllegalArgumentException("doctrines exceed slots");
        return state;
    }
    public String nationId() { return nationId; }
    public synchronized Set<String> completed() { return Set.copyOf(completed); }
    public synchronized List<ResearchQueueEntry> queue() { return List.copyOf(queue); }
    public synchronized int researchSlots() { return researchSlots; }
    public synchronized int doctrineSlots() { return doctrineSlots; }
    public synchronized List<DoctrineSelection> doctrines() { return List.copyOf(doctrines); }
    public synchronized void setDoctrineSlots(int slots) { if (slots < 2 || slots > 3) throw new IllegalArgumentException("doctrine slots must be 2..3"); if (doctrines.size() > slots) throw new IllegalStateException("selected doctrines exceed requested slots"); doctrineSlots = slots; }
    synchronized void selectDoctrine(DoctrineSelection selection) { Objects.requireNonNull(selection); if (doctrines.stream().anyMatch(d -> d.doctrineKey().equals(selection.doctrineKey()))) throw new IllegalStateException("doctrine already selected: " + selection.doctrineKey()); if (doctrines.size() >= doctrineSlots) throw new IllegalStateException("doctrine slots full"); doctrines.add(selection); }
    synchronized DoctrineSelection removeDoctrine(String doctrineKey) { for (int i=0;i<doctrines.size();i++) if (doctrines.get(i).doctrineKey().equals(doctrineKey)) return doctrines.remove(i); throw new IllegalArgumentException("doctrine not selected: " + doctrineKey); }
    public synchronized void setResearchSlots(int slots) { if (slots < 1 || slots > 2) throw new IllegalArgumentException("research slots must be 1..2"); researchSlots = slots; }
    synchronized boolean containsQueued(String researchId) { return queue.stream().anyMatch(e -> e.researchId().equals(researchId)); }
    synchronized void addQueue(ResearchQueueEntry entry) { queue.add(Objects.requireNonNull(entry)); }
    synchronized void replace(ResearchQueueEntry oldEntry, ResearchQueueEntry newEntry) { int i=queue.indexOf(oldEntry); if(i<0) throw new IllegalStateException("queue entry missing"); queue.set(i,newEntry); }
    synchronized ResearchQueueEntry remove(java.util.UUID entryId) { for(int i=0;i<queue.size();i++) if(queue.get(i).entryId().equals(entryId)) return queue.remove(i); throw new IllegalArgumentException("research queue entry not found: "+entryId); }
    synchronized void complete(String researchId) { completed.add(researchId); }
    private static String requireId(String value) { Objects.requireNonNull(value,"nationId"); String v=value.trim().toLowerCase(); if(v.isBlank()) throw new IllegalArgumentException("nationId is blank"); return v; }
}
