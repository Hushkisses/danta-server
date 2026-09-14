package kr.danta.core.snapshot;

import kr.danta.core.nation.NationStatus;

import java.util.Objects;

/** Serializable nation projection used by GameSnapshot schema v2+. */
public record NationSnapshot(
        String nationId,
        String displayName,
        String capitalPointId,
        long treasury,
        NationStatus status
) {
    public NationSnapshot {
        Objects.requireNonNull(nationId, "nationId");
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(status, "status");
        if (nationId.isBlank() || displayName.isBlank()) throw new IllegalArgumentException("blank nation field");
        if (treasury < 0L) throw new IllegalArgumentException("treasury must be >= 0");
    }
}
