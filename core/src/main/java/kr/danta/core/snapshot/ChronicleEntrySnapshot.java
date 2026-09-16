package kr.danta.core.snapshot;

public record ChronicleEntrySnapshot(long runtimeMillis, String type, String summary) {
    public ChronicleEntrySnapshot {
        if (runtimeMillis < 0L) throw new IllegalArgumentException("runtimeMillis must be >= 0");
        if (type == null || type.isBlank()) throw new IllegalArgumentException("type must not be blank");
        if (summary == null || summary.isBlank()) throw new IllegalArgumentException("summary must not be blank");
    }
}
