package kr.danta.core.snapshot;

/** DEV-102 persisted accumulated fame score for one nation. */
public record FameScoreSnapshot(String nationId, long score) {
    public FameScoreSnapshot {
        if (nationId == null || nationId.isBlank()) throw new IllegalArgumentException("nationId must not be blank");
        if (score < 0L) throw new IllegalArgumentException("score must be >= 0");
    }
}
