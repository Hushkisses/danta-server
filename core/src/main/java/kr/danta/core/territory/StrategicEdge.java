package kr.danta.core.territory;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * DEV-022 connection between two strategic points.
 * Edges are undirected at this stage; one edge defines travel in both directions.
 */
public final class StrategicEdge {
    private final String edgeId;
    private final String pointAId;
    private final String pointBId;
    private long baseTravelMillis;
    private final Set<BattlefieldTag> battlefieldTags = new LinkedHashSet<>();

    public StrategicEdge(String edgeId, String pointAId, String pointBId,
                         long baseTravelMillis, Set<BattlefieldTag> battlefieldTags) {
        this.edgeId = requireId(edgeId, "edgeId");
        this.pointAId = requireId(pointAId, "pointAId");
        this.pointBId = requireId(pointBId, "pointBId");
        if (this.pointAId.equals(this.pointBId)) {
            throw new IllegalArgumentException("edge endpoints must be different");
        }
        setBaseTravelMillis(baseTravelMillis);
        setBattlefieldTags(battlefieldTags);
    }

    public StrategicEdge(String edgeId, String pointAId, String pointBId, Duration baseTravelTime) {
        this(edgeId, pointAId, pointBId, Objects.requireNonNull(baseTravelTime).toMillis(), Set.of());
    }

    public String edgeId() { return edgeId; }
    public String pointAId() { return pointAId; }
    public String pointBId() { return pointBId; }

    public synchronized long baseTravelMillis() { return baseTravelMillis; }
    public synchronized Duration baseTravelTime() { return Duration.ofMillis(baseTravelMillis); }
    public synchronized Set<BattlefieldTag> battlefieldTags() { return Set.copyOf(battlefieldTags); }

    public synchronized void setBaseTravelMillis(long baseTravelMillis) {
        if (baseTravelMillis <= 0L) throw new IllegalArgumentException("baseTravelMillis must be > 0");
        this.baseTravelMillis = baseTravelMillis;
    }

    public synchronized void setBattlefieldTags(Set<BattlefieldTag> tags) {
        battlefieldTags.clear();
        if (tags != null) battlefieldTags.addAll(tags);
    }

    public synchronized void addBattlefieldTag(BattlefieldTag tag) {
        battlefieldTags.add(Objects.requireNonNull(tag, "tag"));
    }

    public synchronized void removeBattlefieldTag(BattlefieldTag tag) {
        battlefieldTags.remove(Objects.requireNonNull(tag, "tag"));
    }

    public boolean connects(String pointId) {
        return pointAId.equals(pointId) || pointBId.equals(pointId);
    }

    public String otherPoint(String pointId) {
        if (pointAId.equals(pointId)) return pointBId;
        if (pointBId.equals(pointId)) return pointAId;
        throw new IllegalArgumentException("point is not connected by edge " + edgeId + ": " + pointId);
    }

    public boolean connectsPair(String firstPointId, String secondPointId) {
        return (pointAId.equals(firstPointId) && pointBId.equals(secondPointId))
                || (pointAId.equals(secondPointId) && pointBId.equals(firstPointId));
    }

    private static String requireId(String value, String label) {
        Objects.requireNonNull(value, label);
        String normalized = value.trim();
        if (!normalized.matches("[A-Za-z0-9_-]{1,48}")) {
            throw new IllegalArgumentException(label + " must match [A-Za-z0-9_-]{1,48}");
        }
        return normalized;
    }
}
