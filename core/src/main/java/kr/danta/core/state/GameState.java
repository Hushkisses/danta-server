package kr.danta.core.state;

import kr.danta.core.nation.NationState;
import kr.danta.core.territory.StrategicPoint;
import kr.danta.core.territory.StrategicEdge;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** In-memory holder for the currently active season and its domain state. */
public final class GameState {
    private SeasonState activeSeason;
    private final Map<String, NationState> nations = new LinkedHashMap<>();
    private final Map<String, StrategicPoint> strategicPoints = new LinkedHashMap<>();
    private final Map<String, StrategicEdge> strategicEdges = new LinkedHashMap<>();

    public synchronized Optional<SeasonState> activeSeason() { return Optional.ofNullable(activeSeason); }
    public synchronized boolean hasActiveSeason() { return activeSeason != null; }
    public synchronized void activateSeason(SeasonState season) { activeSeason = Objects.requireNonNull(season); }
    public synchronized void clearActiveSeason() { activeSeason = null; }

    public synchronized void addNation(NationState nation) {
        Objects.requireNonNull(nation, "nation");
        if (nations.containsKey(nation.nationId())) {
            throw new IllegalArgumentException("nation already exists: " + nation.nationId());
        }
        nations.put(nation.nationId(), nation);
    }

    public synchronized Optional<NationState> nation(String nationId) {
        return Optional.ofNullable(nations.get(nationId));
    }

    public synchronized List<NationState> nations() {
        return List.copyOf(new ArrayList<>(nations.values()));
    }

    public synchronized boolean hasNation(String nationId) {
        return nations.containsKey(nationId);
    }

    public synchronized void clearNations() {
        nations.clear();
    }

    public synchronized void addStrategicPoint(StrategicPoint point) {
        Objects.requireNonNull(point, "point");
        if (strategicPoints.containsKey(point.pointId())) {
            throw new IllegalArgumentException("strategic point already exists: " + point.pointId());
        }
        strategicPoints.put(point.pointId(), point);
    }

    public synchronized Optional<StrategicPoint> strategicPoint(String pointId) {
        return Optional.ofNullable(strategicPoints.get(pointId));
    }

    public synchronized List<StrategicPoint> strategicPoints() {
        return List.copyOf(new ArrayList<>(strategicPoints.values()));
    }

    public synchronized boolean hasStrategicPoint(String pointId) {
        return strategicPoints.containsKey(pointId);
    }

    public synchronized void clearStrategicPoints() {
        strategicPoints.clear();
    }

    public synchronized void addStrategicEdge(StrategicEdge edge) {
        Objects.requireNonNull(edge, "edge");
        if (strategicEdges.containsKey(edge.edgeId())) {
            throw new IllegalArgumentException("strategic edge already exists: " + edge.edgeId());
        }
        if (!strategicPoints.containsKey(edge.pointAId()) || !strategicPoints.containsKey(edge.pointBId())) {
            throw new IllegalArgumentException("both edge endpoints must exist before adding edge");
        }
        for (StrategicEdge existing : strategicEdges.values()) {
            if (existing.connectsPair(edge.pointAId(), edge.pointBId())) {
                throw new IllegalArgumentException("an edge already connects " + edge.pointAId() + " and " + edge.pointBId());
            }
        }
        strategicEdges.put(edge.edgeId(), edge);
    }

    public synchronized Optional<StrategicEdge> strategicEdge(String edgeId) {
        return Optional.ofNullable(strategicEdges.get(edgeId));
    }

    public synchronized List<StrategicEdge> strategicEdges() {
        return List.copyOf(new ArrayList<>(strategicEdges.values()));
    }

    public synchronized List<StrategicEdge> edgesForPoint(String pointId) {
        List<StrategicEdge> result = new ArrayList<>();
        for (StrategicEdge edge : strategicEdges.values()) {
            if (edge.connects(pointId)) result.add(edge);
        }
        return List.copyOf(result);
    }

    public synchronized boolean hasStrategicEdge(String edgeId) {
        return strategicEdges.containsKey(edgeId);
    }

    public synchronized void clearStrategicEdges() {
        strategicEdges.clear();
    }
}

