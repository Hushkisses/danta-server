package kr.danta.core.chronicle;

import java.util.Objects;

/** DEV-107 immutable season chronicle entry. */
public record ChronicleEntry(long runtimeMillis, ChronicleEventType type, String summary) {
    public ChronicleEntry {
        if (runtimeMillis < 0L) throw new IllegalArgumentException("runtimeMillis must be >= 0");
        type = Objects.requireNonNull(type, "type");
        summary = Objects.requireNonNull(summary, "summary").trim();
        if (summary.isEmpty()) throw new IllegalArgumentException("summary must not be blank");
    }
}
