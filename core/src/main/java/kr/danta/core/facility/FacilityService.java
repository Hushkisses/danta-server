package kr.danta.core.facility;

import kr.danta.core.state.GameState;

import java.util.*;

/**
 * DEV-080 logical facility/slot rules. Construction timing belongs to DEV-081.
 * Facility effects/costs are deliberately not invented here.
 */
public final class FacilityService {
    private final GameState gameState;
    private final Map<String, LinkedHashMap<String, FacilityState>> byPoint = new LinkedHashMap<>();

    public FacilityService(GameState gameState) { this.gameState = Objects.requireNonNull(gameState); }

    public synchronized FacilityState buildTierOne(String pointId, String facilityId) {
        var point = gameState.strategicPoint(pointId).orElseThrow(() -> new IllegalArgumentException("point not found: " + pointId));
        var facilities = byPoint.computeIfAbsent(pointId, ignored -> new LinkedHashMap<>());
        if (facilities.containsKey(facilityId)) throw new IllegalStateException("facility already exists: " + facilityId);
        if (facilities.size() >= point.facilitySlots()) throw new IllegalStateException("facility slots full: " + pointId);
        FacilityState state = new FacilityState(facilityId, FacilityTier.I);
        facilities.put(facilityId, state);
        return state;
    }

    public synchronized FacilityState upgrade(String pointId, String facilityId) {
        var facilities = byPoint.get(pointId);
        if (facilities == null || !facilities.containsKey(facilityId)) throw new IllegalArgumentException("facility not found: " + facilityId);
        FacilityState current = facilities.get(facilityId);
        FacilityState upgraded = new FacilityState(facilityId, current.tier().next());
        facilities.put(facilityId, upgraded);
        return upgraded;
    }

    public synchronized Optional<FacilityState> facility(String pointId, String facilityId) {
        Objects.requireNonNull(facilityId, "facilityId");
        var facilities = byPoint.get(pointId);
        return facilities == null ? Optional.empty() : Optional.ofNullable(facilities.get(facilityId));
    }

    /** Restore authoritative installed state without consuming construction time again. */
    public synchronized void restore(String pointId, FacilityState state) {
        Objects.requireNonNull(state, "state");
        var point = gameState.strategicPoint(pointId).orElseThrow(() -> new IllegalArgumentException("point not found: " + pointId));
        var facilities = byPoint.computeIfAbsent(pointId, ignored -> new LinkedHashMap<>());
        if (facilities.containsKey(state.facilityId())) throw new IllegalArgumentException("facility already exists: " + state.facilityId());
        if (facilities.size() >= point.facilitySlots()) throw new IllegalStateException("facility slots full: " + pointId);
        facilities.put(state.facilityId(), state);
    }

    public synchronized void clear() { byPoint.clear(); }

    public synchronized List<FacilityState> facilities(String pointId) {
        var facilities = byPoint.get(pointId);
        return facilities == null ? List.of() : List.copyOf(facilities.values());
    }

    public synchronized int availableSlots(String pointId) {
        var point = gameState.strategicPoint(pointId).orElseThrow(() -> new IllegalArgumentException("point not found: " + pointId));
        return point.facilitySlots() - facilities(pointId).size();
    }
}
