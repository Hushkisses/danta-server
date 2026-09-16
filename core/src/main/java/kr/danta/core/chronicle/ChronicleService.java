package kr.danta.core.chronicle;

import java.util.ArrayList;
import java.util.List;

/** DEV-107 in-memory authoritative chronicle ordered by server runtime. */
public final class ChronicleService {
    private final List<ChronicleEntry> entries = new ArrayList<>();

    public synchronized ChronicleEntry record(long runtimeMillis, ChronicleEventType type, String summary) {
        ChronicleEntry entry = new ChronicleEntry(runtimeMillis, type, summary);
        entries.add(entry);
        return entry;
    }

    public synchronized List<ChronicleEntry> entries() {
        return List.copyOf(entries);
    }

    public synchronized List<ChronicleEntry> recent(int limit) {
        if (limit <= 0) return List.of();
        int from = Math.max(0, entries.size() - limit);
        return List.copyOf(entries.subList(from, entries.size()));
    }

    public synchronized void restore(List<ChronicleEntry> restored) {
        entries.clear();
        if (restored != null) entries.addAll(restored);
    }

    public synchronized void clear() { entries.clear(); }
}
