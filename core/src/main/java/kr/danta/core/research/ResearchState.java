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

    public ResearchState(String nationId) { this.nationId = requireId(nationId); }
    public String nationId() { return nationId; }
    public synchronized Set<String> completed() { return Set.copyOf(completed); }
    public synchronized List<ResearchQueueEntry> queue() { return List.copyOf(queue); }
    public synchronized int researchSlots() { return researchSlots; }
    public synchronized void setResearchSlots(int slots) { if (slots < 1 || slots > 2) throw new IllegalArgumentException("research slots must be 1..2"); researchSlots = slots; }
    synchronized boolean containsQueued(String researchId) { return queue.stream().anyMatch(e -> e.researchId().equals(researchId)); }
    synchronized void addQueue(ResearchQueueEntry entry) { queue.add(Objects.requireNonNull(entry)); }
    synchronized void replace(ResearchQueueEntry oldEntry, ResearchQueueEntry newEntry) { int i=queue.indexOf(oldEntry); if(i<0) throw new IllegalStateException("queue entry missing"); queue.set(i,newEntry); }
    synchronized ResearchQueueEntry remove(java.util.UUID entryId) { for(int i=0;i<queue.size();i++) if(queue.get(i).entryId().equals(entryId)) return queue.remove(i); throw new IllegalArgumentException("research queue entry not found: "+entryId); }
    synchronized void complete(String researchId) { completed.add(researchId); }
    private static String requireId(String value) { Objects.requireNonNull(value,"nationId"); String v=value.trim().toLowerCase(); if(v.isBlank()) throw new IllegalArgumentException("nationId is blank"); return v; }
}
