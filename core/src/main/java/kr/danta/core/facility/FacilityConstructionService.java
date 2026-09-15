package kr.danta.core.facility;

import kr.danta.core.runtime.RuntimeScheduledTask;
import kr.danta.core.runtime.RuntimeScheduler;
import kr.danta.core.state.GameState;

import java.time.Duration;
import java.util.*;

/**
 * DEV-081 construction scheduling over the existing server-runtime scheduler.
 * Durations are supplied by the caller/data layer; no balance duration is invented here.
 */
public final class FacilityConstructionService {
    public static final String TASK_TYPE = "facility.construction.complete";

    private final GameState gameState;
    private final FacilityService facilityService;
    private final RuntimeScheduler scheduler;
    private final Map<UUID, FacilityConstructionState> pendingById = new LinkedHashMap<>();

    public FacilityConstructionService(GameState gameState, FacilityService facilityService, RuntimeScheduler scheduler) {
        this.gameState = Objects.requireNonNull(gameState, "gameState");
        this.facilityService = Objects.requireNonNull(facilityService, "facilityService");
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
    }

    public synchronized FacilityConstructionState scheduleBuild(String pointId, String facilityId, Duration duration) {
        validateDuration(duration);
        var point = gameState.strategicPoint(pointId)
                .orElseThrow(() -> new IllegalArgumentException("point not found: " + pointId));
        if (facilityService.facility(pointId, facilityId).isPresent()) throw new IllegalStateException("facility already exists: " + facilityId);
        ensureNoPending(pointId, facilityId);
        long reservedNewSlots = pendingById.values().stream()
                .filter(p -> p.pointId().equals(pointId) && p.targetTier() == FacilityTier.I).count();
        if (facilityService.facilities(pointId).size() + reservedNewSlots >= point.facilitySlots())
            throw new IllegalStateException("facility slots full: " + pointId);
        return schedule(pointId, facilityId, FacilityTier.I, duration);
    }

    public synchronized FacilityConstructionState scheduleUpgrade(String pointId, String facilityId, Duration duration) {
        validateDuration(duration);
        ensureNoPending(pointId, facilityId);
        FacilityState current = facilityService.facility(pointId, facilityId)
                .orElseThrow(() -> new IllegalArgumentException("facility not found: " + facilityId));
        return schedule(pointId, facilityId, current.tier().next(), duration);
    }

    private FacilityConstructionState schedule(String pointId, String facilityId, FacilityTier targetTier, Duration duration) {
        UUID constructionId = UUID.randomUUID();
        RuntimeScheduledTask task = scheduler.scheduleAfter(duration, TASK_TYPE, payload(constructionId, pointId, facilityId, targetTier));
        FacilityConstructionState pending = new FacilityConstructionState(
                constructionId, pointId, facilityId, targetTier, task.dueRuntimeMillis());
        pendingById.put(constructionId, pending);
        return pending;
    }

    public synchronized void restore(FacilityConstructionState pending) {
        Objects.requireNonNull(pending, "pending");
        if (pendingById.containsKey(pending.constructionId())) throw new IllegalArgumentException("duplicate construction id");
        ensureNoPending(pending.pointId(), pending.facilityId());
        pendingById.put(pending.constructionId(), pending);
        scheduler.restore(new RuntimeScheduledTask(pending.constructionId(), pending.dueRuntimeMillis(), TASK_TYPE,
                payload(pending.constructionId(), pending.pointId(), pending.facilityId(), pending.targetTier())));
    }

    public synchronized FacilityState complete(UUID constructionId) {
        FacilityConstructionState pending = pendingById.remove(Objects.requireNonNull(constructionId, "constructionId"));
        if (pending == null) throw new IllegalStateException("construction not pending: " + constructionId);
        if (pending.targetTier() == FacilityTier.I) return facilityService.buildTierOne(pending.pointId(), pending.facilityId());
        FacilityState current = facilityService.facility(pending.pointId(), pending.facilityId())
                .orElseThrow(() -> new IllegalStateException("facility missing at completion: " + pending.facilityId()));
        if (current.tier().level() + 1 != pending.targetTier().level())
            throw new IllegalStateException("facility tier changed while construction was pending");
        return facilityService.upgrade(pending.pointId(), pending.facilityId());
    }

    public synchronized boolean cancel(UUID constructionId) {
        FacilityConstructionState removed = pendingById.remove(Objects.requireNonNull(constructionId, "constructionId"));
        return removed != null && scheduler.cancel(constructionId);
    }

    public synchronized List<FacilityConstructionState> pending() { return List.copyOf(pendingById.values()); }

    private void ensureNoPending(String pointId, String facilityId) {
        if (pendingById.values().stream().anyMatch(p -> p.pointId().equals(pointId) && p.facilityId().equals(facilityId)))
            throw new IllegalStateException("facility construction already pending: " + facilityId);
    }

    private static void validateDuration(Duration duration) {
        Objects.requireNonNull(duration, "duration");
        if (duration.isNegative()) throw new IllegalArgumentException("duration must be >= 0");
    }

    private static Map<String, String> payload(UUID id, String pointId, String facilityId, FacilityTier tier) {
        return Map.of("constructionId", id.toString(), "pointId", pointId, "facilityId", facilityId, "targetTier", tier.name());
    }
}
