package kr.danta.paper.persistence;

import kr.danta.core.nation.NationState;
import kr.danta.core.persistence.AsyncKeyValueRepository;
import kr.danta.core.runtime.RuntimeClockService;
import kr.danta.core.snapshot.GameSnapshot;
import kr.danta.core.snapshot.GameSnapshotCodec;
import kr.danta.core.snapshot.NationSnapshot;
import kr.danta.core.snapshot.StrategicPointSnapshot;
import kr.danta.core.snapshot.StrategicEdgeSnapshot;
import kr.danta.core.state.GameState;
import kr.danta.core.state.SeasonState;
import kr.danta.core.territory.PointPosition;
import kr.danta.core.territory.StrategicPoint;
import kr.danta.core.territory.StrategicEdge;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/** DEV-017: periodic snapshot + explicit important flush + restart recovery. */
public final class SnapshotService {
    private static final String NAMESPACE = "snapshot";
    private static final String ACTIVE_KEY = "active";
    private static final Duration SHUTDOWN_FLUSH_TIMEOUT = Duration.ofSeconds(5);

    private final AsyncKeyValueRepository repository;
    private final RuntimeClockService runtimeClock;
    private final GameState gameState;
    private final Logger logger;

    public SnapshotService(AsyncKeyValueRepository repository, RuntimeClockService runtimeClock,
                           GameState gameState, Logger logger) {
        this.repository = repository;
        this.runtimeClock = runtimeClock;
        this.gameState = gameState;
        this.logger = logger;
    }

    public CompletableFuture<Void> saveAsync(String reason) {
        GameSnapshot snapshot = capture();
        return repository.save(NAMESPACE, ACTIVE_KEY, GameSnapshotCodec.encode(snapshot))
                .whenComplete((ignored, error) -> {
                    if (error == null) logger.info("DEV-017 snapshot saved: reason=" + reason
                            + ", runtimeMs=" + snapshot.runtimeElapsedMillis());
                    else logger.warning("DEV-017 snapshot save failed: reason=" + reason + ", error=" + error.getMessage());
                });
    }

    /** Important-event flush. It is still DB-async; callers do not block the Paper main thread. */
    public CompletableFuture<Void> flushImportantAsync(String reason) {
        return saveAsync("IMPORTANT:" + reason);
    }

    public CompletableFuture<Optional<GameSnapshot>> loadAsync() {
        return repository.find(NAMESPACE, ACTIVE_KEY).thenApply(value -> value.map(GameSnapshotCodec::decode));
    }

    public void apply(GameSnapshot snapshot) {
        runtimeClock.setElapsedMillis(snapshot.runtimeElapsedMillis());
        runtimeClock.setSpeedMultiplier(snapshot.runtimeSpeedMultiplier());
        if (snapshot.runtimePaused()) runtimeClock.pause(); else runtimeClock.resume();
        gameState.clearActiveSeason();
        if (snapshot.seasonId() != null) {
            gameState.activateSeason(new SeasonState(snapshot.seasonId(), snapshot.seasonDisplayName()));
        }
        gameState.clearNations();
        for (NationSnapshot nation : snapshot.nations()) {
            gameState.addNation(new NationState(nation.nationId(), nation.displayName(), nation.capitalPointId(),
                    nation.treasury(), nation.status()));
        }
        gameState.clearStrategicEdges();
        gameState.clearStrategicPoints();
        for (StrategicPointSnapshot point : snapshot.strategicPoints()) {
            gameState.addStrategicPoint(new StrategicPoint(
                    point.pointId(), point.displayName(), point.type(), point.ownerNationId(),
                    new PointPosition(point.worldName(), point.x(), point.y(), point.z()),
                    point.facilitySlots(), point.baseProductionPerHour()));
        }
        for (StrategicEdgeSnapshot edge : snapshot.strategicEdges()) {
            gameState.addStrategicEdge(new StrategicEdge(edge.edgeId(), edge.pointAId(), edge.pointBId(),
                    edge.baseTravelMillis(), edge.battlefieldTags()));
        }
    }

    public void flushOnShutdown() {
        try {
            saveAsync("shutdown").get(SHUTDOWN_FLUSH_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception ex) {
            logger.warning("DEV-017 shutdown snapshot did not complete within 5s: " + ex.getMessage());
        }
    }

    private GameSnapshot capture() {
        Optional<SeasonState> season = gameState.activeSeason();
        List<NationSnapshot> nations = gameState.nations().stream()
                .map(nation -> new NationSnapshot(nation.nationId(), nation.displayName(),
                        nation.capitalPointId().orElse(null), nation.treasury(), nation.status()))
                .toList();
        List<StrategicPointSnapshot> points = gameState.strategicPoints().stream()
                .map(point -> new StrategicPointSnapshot(
                        point.pointId(), point.displayName(), point.type(), point.ownerNationId().orElse(null),
                        point.position().worldName(), point.position().x(), point.position().y(), point.position().z(),
                        point.facilitySlots(), point.baseProductionPerHour()))
                .toList();
        List<StrategicEdgeSnapshot> edges = gameState.strategicEdges().stream()
                .map(edge -> new StrategicEdgeSnapshot(edge.edgeId(), edge.pointAId(), edge.pointBId(),
                        edge.baseTravelMillis(), edge.battlefieldTags()))
                .toList();
        return new GameSnapshot(GameSnapshot.CURRENT_SCHEMA, System.currentTimeMillis(),
                runtimeClock.elapsedMillis(), runtimeClock.isPaused(), runtimeClock.speedMultiplier(),
                season.map(SeasonState::seasonId).orElse(null), season.map(SeasonState::displayName).orElse(null),
                nations, points, edges);
    }
}
