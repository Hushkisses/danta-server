package kr.danta.paper.persistence;

import kr.danta.core.army.ArmyState;
import kr.danta.core.army.ArmyOrder;
import kr.danta.core.army.ArmyRoute;
import kr.danta.core.nation.NationState;
import kr.danta.core.nation.VassalRelation;
import kr.danta.core.nation.VassalService;
import kr.danta.core.nation.IndependenceWarService;
import kr.danta.core.nation.IndependenceWarState;
import kr.danta.core.snapshot.IndependenceWarSnapshot;
import kr.danta.core.snapshot.FameScoreSnapshot;
import kr.danta.core.snapshot.ChronicleEntrySnapshot;
import kr.danta.core.chronicle.ChronicleEntry;
import kr.danta.core.chronicle.ChronicleEventType;
import kr.danta.core.chronicle.ChronicleService;
import kr.danta.core.diplomacy.DiplomacyService;
import kr.danta.core.diplomacy.DiplomaticRelation;
import kr.danta.core.snapshot.DiplomaticRelationSnapshot;
import kr.danta.core.snapshot.VassalRelationSnapshot;
import kr.danta.core.economy.PersonalWallet;
import kr.danta.core.economy.StrategicResourceStockpile;
import kr.danta.core.economy.LocalResourceStockpile;
import kr.danta.core.persistence.AsyncKeyValueRepository;
import kr.danta.core.runtime.RuntimeClockService;
import kr.danta.core.snapshot.GameSnapshot;
import kr.danta.core.snapshot.GameSnapshotCodec;
import kr.danta.core.snapshot.SiegeInstanceSnapshot;
import kr.danta.core.snapshot.SiegeReservationSnapshot;
import kr.danta.core.snapshot.SiegeRuntimeSnapshot;
import kr.danta.core.siege.SiegeService;
import kr.danta.core.siege.SiegeReservation;
import kr.danta.core.siege.SiegeReservationService;
import kr.danta.paper.siege.DantaSiegeRuntime;
import kr.danta.core.snapshot.ArmySnapshot;
import kr.danta.core.snapshot.ArmyOrderSnapshot;
import kr.danta.core.snapshot.ArmyOperationQueueSnapshot;
import kr.danta.core.snapshot.NationSnapshot;
import kr.danta.core.snapshot.PersonalWalletSnapshot;
import kr.danta.core.snapshot.StrategicResourceStockpileSnapshot;
import kr.danta.core.snapshot.LocalResourceStockpileSnapshot;
import kr.danta.core.snapshot.StrategicPointSnapshot;
import kr.danta.core.snapshot.StrategicEdgeSnapshot;
import kr.danta.core.state.GameState;
import kr.danta.core.general.GeneralState;
import kr.danta.core.general.GeneralStats;
import kr.danta.core.general.GeneralTrait;
import kr.danta.core.general.GeneralAbility;
import kr.danta.core.general.GeneralRecoveryState;
import kr.danta.core.general.GeneralCaptivityState;
import kr.danta.core.general.GeneralHealthStatus;
import kr.danta.core.snapshot.GeneralSnapshot;
import kr.danta.core.snapshot.FacilitySnapshot;
import kr.danta.core.snapshot.FacilityConstructionSnapshot;
import kr.danta.core.snapshot.ResearchStateSnapshot;
import kr.danta.core.snapshot.ResearchQueueSnapshot;
import kr.danta.core.facility.FacilityService;
import kr.danta.core.facility.FacilityState;
import kr.danta.core.facility.FacilityConstructionService;
import kr.danta.core.research.ResearchQueueEntry;
import kr.danta.core.research.ResearchService;
import kr.danta.core.research.ResearchState;
import kr.danta.core.facility.FacilityConstructionState;
import kr.danta.core.state.SeasonState;
import kr.danta.core.territory.PointPosition;
import kr.danta.core.territory.StrategicPoint;
import kr.danta.core.territory.StrategicEdge;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/** DEV-017: periodic snapshot + explicit important flush + restart recovery. */
public final class SnapshotService {
    private static final String NAMESPACE = "snapshot";
    private static final String ACTIVE_KEY = "active";
    private static final String BACKUP_KEY = "backup";
    private static final Duration SHUTDOWN_FLUSH_TIMEOUT = Duration.ofSeconds(5);

    private final AsyncKeyValueRepository repository;
    private final RuntimeClockService runtimeClock;
    private final GameState gameState;
    private final Logger logger;
    private volatile FacilityService facilityService;
    private volatile FacilityConstructionService facilityConstructionService;
    private volatile ResearchService researchService;
    private volatile DiplomacyService diplomacyService;
    private volatile VassalService vassalService;
    private volatile IndependenceWarService independenceWarService;
    private volatile ChronicleService chronicleService;
    private volatile SiegeService siegeService;
    private volatile SiegeReservationService siegeReservationService;
    private volatile DantaSiegeRuntime siegeRuntime;
    private volatile List<ArmyOrderSnapshot> restoredArmyOrders = List.of();
    private volatile List<ArmyOperationQueueSnapshot> armyOperationQueues = List.of();

    public SnapshotService(AsyncKeyValueRepository repository, RuntimeClockService runtimeClock,
                           GameState gameState, Logger logger) {
        this.repository = repository;
        this.runtimeClock = runtimeClock;
        this.gameState = gameState;
        this.logger = logger;
    }

    public void bindVassals(VassalService vassalService) { this.vassalService = Objects.requireNonNull(vassalService, "vassalService"); }
    public void bindIndependenceWars(IndependenceWarService service) { this.independenceWarService = Objects.requireNonNull(service, "independenceWarService"); }
    public void bindChronicle(ChronicleService service) { this.chronicleService = Objects.requireNonNull(service, "chronicleService"); }

    public void bindSieges(SiegeService siegeService,
                           SiegeReservationService siegeReservationService,
                           DantaSiegeRuntime siegeRuntime) {
        this.siegeService = Objects.requireNonNull(siegeService, "siegeService");
        this.siegeReservationService = Objects.requireNonNull(siegeReservationService, "siegeReservationService");
        this.siegeRuntime = Objects.requireNonNull(siegeRuntime, "siegeRuntime");
    }

    public void bindDiplomacy(DiplomacyService diplomacyService) { this.diplomacyService = Objects.requireNonNull(diplomacyService, "diplomacyService"); }

    public void bindResearch(ResearchService researchService) {
        this.researchService = Objects.requireNonNull(researchService, "researchService");
    }

    public void bindFacilities(FacilityService facilityService, FacilityConstructionService constructionService) {
        this.facilityService = Objects.requireNonNull(facilityService, "facilityService");
        this.facilityConstructionService = Objects.requireNonNull(constructionService, "constructionService");
    }

    public CompletableFuture<Void> saveAsync(String reason) {
        GameSnapshot snapshot = capture();
        if (snapshot.nations().isEmpty() && snapshot.strategicPoints().isEmpty()) {
            logger.warning("DEV-017 snapshot save blocked: refusing to overwrite persistent state with an empty GameState, reason=" + reason);
            return CompletableFuture.failedFuture(new IllegalStateException("refusing to save empty game state"));
        }
        String encoded = GameSnapshotCodec.encode(snapshot);
        return repository.find(NAMESPACE, ACTIVE_KEY)
                .thenCompose(previous -> {
                    CompletableFuture<Void> backup = previous.isPresent()
                            ? repository.save(NAMESPACE, BACKUP_KEY, previous.get())
                            : CompletableFuture.completedFuture(null);
                    return backup.thenCompose(ignored -> repository.save(NAMESPACE, ACTIVE_KEY, encoded));
                })
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

    public CompletableFuture<Optional<GameSnapshot>> loadBackupAsync() {
        return repository.find(NAMESPACE, BACKUP_KEY).thenApply(value -> value.map(GameSnapshotCodec::decode));
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
        gameState.clearPersonalWallets();
        for (PersonalWalletSnapshot wallet : snapshot.personalWallets()) {
            gameState.addPersonalWallet(new PersonalWallet(wallet.playerId(), wallet.balance()));
        }
        gameState.clearStrategicResourceStockpiles();
        for (StrategicResourceStockpileSnapshot resources : snapshot.strategicResourceStockpiles()) {
            gameState.addStrategicResourceStockpile(new StrategicResourceStockpile(resources.nationId(), resources.amounts()));
        }
        gameState.clearLocalResourceStockpiles();
        gameState.clearStrategicEdges();
        gameState.clearStrategicPoints();
        for (StrategicPointSnapshot point : snapshot.strategicPoints()) {
            gameState.addStrategicPoint(new StrategicPoint(
                    point.pointId(), point.displayName(), point.type(), point.ownerNationId(),
                    new PointPosition(point.worldName(), point.x(), point.y(), point.z()),
                    point.facilitySlots(), point.baseProductionPerHour()));
        }
        if (siegeService != null && siegeReservationService != null && siegeRuntime != null) {
            siegeReservationService.clear();
            siegeService.clear();
            for (SiegeInstanceSnapshot siege : snapshot.siegeRuntime().instances()) {
                siegeService.restore(
                        siege.siegeId(), siege.pointId(), siege.attackerNationId(), siege.defenderNationId(),
                        siege.phase(), siege.result());
            }
            for (SiegeReservationSnapshot reservation : snapshot.siegeRuntime().reservations()) {
                siegeReservationService.restore(new SiegeReservation(
                        reservation.siegeId(),
                        Instant.ofEpochMilli(reservation.scheduledAtEpochMillis()),
                        reservation.confirmedAtEpochMillis() == null
                                ? null : Instant.ofEpochMilli(reservation.confirmedAtEpochMillis())));
            }
            siegeRuntime.restoreState(snapshot.siegeRuntime());
        }
        // Relations reference restored nations (and vassalization also depends on nation state), so restore them only after nations/points exist.
        if (diplomacyService != null) {
            diplomacyService.clear();
            for (DiplomaticRelationSnapshot relation : snapshot.diplomaticRelations()) {
                diplomacyService.restore(new DiplomaticRelation(relation.nationAId(), relation.nationBId(), relation.status()));
            }
        }
        if (vassalService != null) {
            vassalService.clear();
            for (VassalRelationSnapshot relation : snapshot.vassalRelations()) {
                vassalService.restore(new VassalRelation(relation.vassalNationId(), relation.overlordNationId(), relation.vassalizedAtRuntimeMillis()));
            }
        }
        if (independenceWarService != null) {
            independenceWarService.clear();
            for (IndependenceWarSnapshot war : snapshot.independenceWars()) independenceWarService.restore(
                    new IndependenceWarState(war.vassalNationId(), war.overlordNationId(), war.declaredAtRuntimeMillis(),
                            war.holdUntilRuntimeMillis(), war.redeclareAfterRuntimeMillis(), war.active()));
        }
        for (StrategicEdgeSnapshot edge : snapshot.strategicEdges()) {
            gameState.addStrategicEdge(new StrategicEdge(edge.edgeId(), edge.pointAId(), edge.pointBId(),
                    edge.baseTravelMillis(), edge.battlefieldTags()));
        }
        // Local stockpiles reference strategic points, so restore them only after points exist.
        for (LocalResourceStockpileSnapshot local : snapshot.localResourceStockpiles()) {
            gameState.addLocalResourceStockpile(new LocalResourceStockpile(local.pointId(), local.amounts()));
        }
        gameState.clearArmies();
        for (ArmySnapshot army : snapshot.armies()) {
            gameState.addArmy(new ArmyState(army.armyId(), army.ownerNationId(), army.locationPointId(),
                    army.status(), army.troopComposition(), army.expeditionSupplyLevel(), army.carriedFood()));
        }
        gameState.clearGenerals();
        for (GeneralSnapshot general : snapshot.generals()) {
            GeneralState restored = new GeneralState(general.generalId(), general.ownerNationId(), general.grade(), general.level(),
                    new GeneralStats(general.command(), general.martial(), general.strategy(), general.logistics()));
            general.traitIds().forEach(id -> restored.addTrait(new GeneralTrait(id)));
            general.abilityIds().forEach(id -> restored.addAbility(new GeneralAbility(id)));
            if (general.healthStatus() != GeneralHealthStatus.HEALTHY
                    && general.injuredAtRuntimeMillis() != null && general.recoveryReadyAtRuntimeMillis() != null) {
                restored.setRecoveryState(new GeneralRecoveryState(general.healthStatus(),
                        general.injuredAtRuntimeMillis(), general.recoveryReadyAtRuntimeMillis()));
            }
            if (general.captorNationId() != null && general.capturedAtRuntimeMillis() != null
                    && general.detentionEndsAtRuntimeMillis() != null) {
                restored.setCaptivityState(new GeneralCaptivityState(general.captorNationId(),
                        general.capturedAtRuntimeMillis(), general.detentionEndsAtRuntimeMillis()));
            }
            gameState.addGeneral(restored);
        }
        if (facilityService != null) {
            facilityService.clear();
            for (FacilitySnapshot facility : snapshot.facilities()) {
                facilityService.restore(facility.pointId(), new FacilityState(facility.facilityId(), facility.tier()));
            }
        }
        if (facilityConstructionService != null) {
            for (FacilityConstructionSnapshot pending : snapshot.facilityConstructions()) {
                facilityConstructionService.restore(new FacilityConstructionState(
                        pending.constructionId(), pending.pointId(), pending.facilityId(),
                        pending.targetTier(), pending.dueRuntimeMillis()));
            }
        }
        if (researchService != null) {
            for (ResearchStateSnapshot research : snapshot.researchStates()) {
                List<ResearchQueueEntry> queue = research.queue().stream()
                        .map(q -> new ResearchQueueEntry(q.entryId(), q.researchId(), q.taskId(), q.dueRuntimeMillis()))
                        .toList();
                researchService.restore(ResearchState.restored(
                        research.nationId(), research.researchSlots(), research.completed(), queue, research.doctrineSlots(), research.doctrines()));
            }
        }
        if (chronicleService != null) {
            chronicleService.restore(snapshot.chronicleEntries().stream()
                    .map(e -> new ChronicleEntry(e.runtimeMillis(), ChronicleEventType.valueOf(e.type()), e.summary()))
                    .toList());
        }
        gameState.clearFameScores();
        for (FameScoreSnapshot fame : snapshot.fameScores()) {
            gameState.restoreFameScore(fame.nationId(), fame.score());
        }
        // Restore assignments after points, armies and generals all exist.
        for (GeneralSnapshot general : snapshot.generals()) {
            if (general.commandedArmyId() != null) {
                gameState.army(general.commandedArmyId()).ifPresent(a -> a.setCommanderGeneralId(general.generalId()));
            } else if (general.assignedPointId() != null) {
                gameState.strategicPoint(general.assignedPointId()).ifPresent(point -> point.setAssignedGeneralId(general.generalId()));
            }
        }
        restoredArmyOrders = snapshot.armyOrders();
        armyOperationQueues = snapshot.armyOperationQueues();
        for (ArmyOrderSnapshot order : snapshot.armyOrders()) {
            gameState.addArmyOrder(new ArmyOrder(order.orderId(), order.armyId(), order.type(),
                    new ArmyRoute(order.originPointId(), order.destinationPointId(), order.edgeId()), order.status()));
        }
    }

    public List<ArmyOrderSnapshot> restoredArmyOrders() {
        return List.copyOf(restoredArmyOrders);
    }

    public void setActiveArmyMovements(List<ArmyOrderSnapshot> movements) {
        restoredArmyOrders = movements == null ? List.of() : List.copyOf(movements);
    }

    public List<ArmyOperationQueueSnapshot> armyOperationQueues() { return List.copyOf(armyOperationQueues); }
    public void setArmyOperationQueues(List<ArmyOperationQueueSnapshot> queues) {
        armyOperationQueues = queues == null ? List.of() : List.copyOf(queues);
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
        List<ArmySnapshot> armies = gameState.armies().stream()
                .map(army -> new ArmySnapshot(army.armyId(), army.ownerNationId(), army.locationPointId(),
                        army.status(), army.baseTroops(), army.expeditionSupplyLevel(), army.carriedFood(),
                        army.troopComposition()))
                .toList();
        java.util.Map<String, Long> dueByArmy = restoredArmyOrders.stream()
                .collect(java.util.stream.Collectors.toMap(ArmyOrderSnapshot::armyId, ArmyOrderSnapshot::dueRuntimeMillis, (a, b) -> b));
        List<ArmyOrderSnapshot> armyOrders = gameState.armyOrders().stream()
                .map(order -> new ArmyOrderSnapshot(order.orderId(), order.armyId(), order.type(),
                        order.route().originPointId(), order.route().destinationPointId(), order.route().edgeId(),
                        order.status(), dueByArmy.getOrDefault(order.armyId(), runtimeClock.elapsedMillis())))
                .toList();
        List<PersonalWalletSnapshot> wallets = gameState.personalWallets().stream()
                .map(wallet -> new PersonalWalletSnapshot(wallet.playerId(), wallet.balance()))
                .toList();
        List<StrategicResourceStockpileSnapshot> resources = gameState.strategicResourceStockpiles().stream()
                .map(stockpile -> new StrategicResourceStockpileSnapshot(stockpile.nationId(), stockpile.amounts()))
                .toList();
        List<LocalResourceStockpileSnapshot> localResources = gameState.localResourceStockpiles().stream()
                .map(stockpile -> new LocalResourceStockpileSnapshot(stockpile.pointId(), stockpile.amounts()))
                .toList();
        List<GeneralSnapshot> generals = gameState.generals().stream().map(general -> {
            var recovery = general.recoveryState().orElse(null);
            var captivity = general.captivityState().orElse(null);
            return new GeneralSnapshot(general.generalId(), general.ownerNationId(), general.grade(), general.level(),
                    general.stats().command(), general.stats().martial(), general.stats().strategy(), general.stats().logistics(),
                    general.traits().stream().map(GeneralTrait::traitId).toList(),
                    general.abilities().stream().map(GeneralAbility::abilityId).toList(),
                    gameState.commandedArmyId(general.generalId()).orElse(null),
                    gameState.assignedPointId(general.generalId()).orElse(null),
                    general.healthStatus(),
                    recovery == null ? null : recovery.injuredAtRuntimeMillis(),
                    recovery == null ? null : recovery.recoveryReadyAtRuntimeMillis(),
                    captivity == null ? null : captivity.captorNationId(),
                    captivity == null ? null : captivity.capturedAtRuntimeMillis(),
                    captivity == null ? null : captivity.detentionEndsAtRuntimeMillis());
        }).toList();
        List<FacilitySnapshot> facilities = facilityService == null ? List.of() :
                gameState.strategicPoints().stream()
                        .flatMap(point -> facilityService.facilities(point.pointId()).stream()
                                .map(f -> new FacilitySnapshot(point.pointId(), f.facilityId(), f.tier())))
                        .toList();
        List<FacilityConstructionSnapshot> facilityConstructions = facilityConstructionService == null ? List.of() :
                facilityConstructionService.pending().stream()
                        .map(p -> new FacilityConstructionSnapshot(p.constructionId(), p.pointId(), p.facilityId(),
                                p.targetTier(), p.dueRuntimeMillis()))
                        .toList();
        List<ResearchStateSnapshot> researchStates = researchService == null ? List.of() :
                researchService.states().stream()
                        .map(state -> new ResearchStateSnapshot(state.nationId(), state.researchSlots(), state.completed(),
                                state.queue().stream().map(q -> new ResearchQueueSnapshot(
                                        q.entryId(), q.researchId(), q.taskId(), q.dueRuntimeMillis())).toList(), state.doctrineSlots(), state.doctrines()))
                        .toList();
        List<DiplomaticRelationSnapshot> diplomaticRelations = diplomacyService == null ? List.of() : diplomacyService.relations().stream().map(r -> new DiplomaticRelationSnapshot(r.nationAId(), r.nationBId(), r.status())).toList();
        List<VassalRelationSnapshot> vassalRelations = vassalService == null ? List.of() : vassalService.relations().stream().map(r -> new VassalRelationSnapshot(r.vassalNationId(), r.overlordNationId(), r.vassalizedAtRuntimeMillis())).toList();
        List<IndependenceWarSnapshot> independenceWars = independenceWarService == null ? List.of() : independenceWarService.states().stream().map(w -> new IndependenceWarSnapshot(w.vassalNationId(), w.overlordNationId(), w.declaredAtRuntimeMillis(), w.holdUntilRuntimeMillis(), w.redeclareAfterRuntimeMillis(), w.active())).toList();
        List<FameScoreSnapshot> fameScores = gameState.fameScores().entrySet().stream()
                .map(e -> new FameScoreSnapshot(e.getKey(), e.getValue())).toList();
        List<ChronicleEntrySnapshot> chronicleEntries = chronicleService == null ? List.of()
                : chronicleService.entries().stream()
                .map(e -> new ChronicleEntrySnapshot(e.runtimeMillis(), e.type().name(), e.summary())).toList();
        SiegeRuntimeSnapshot paperSiege = siegeRuntime == null ? SiegeRuntimeSnapshot.empty() : siegeRuntime.snapshotState();
        List<SiegeInstanceSnapshot> siegeInstances = siegeService == null ? List.of()
                : siegeService.instances().stream()
                .map(x -> new SiegeInstanceSnapshot(
                        x.siegeId(), x.pointId(), x.attackerNationId(), x.defenderNationId(), x.phase(), x.result()))
                .toList();
        List<SiegeReservationSnapshot> siegeReservations = siegeReservationService == null ? List.of()
                : siegeReservationService.reservations().stream()
                .map(x -> new SiegeReservationSnapshot(
                        x.siegeId(), x.scheduledAt().toEpochMilli(),
                        x.confirmedAt() == null ? null : x.confirmedAt().toEpochMilli()))
                .toList();
        SiegeRuntimeSnapshot siegeState = new SiegeRuntimeSnapshot(
                siegeInstances, siegeReservations, paperSiege.progress(), paperSiege.participants(), paperSiege.morale());
        return new GameSnapshot(GameSnapshot.CURRENT_SCHEMA, System.currentTimeMillis(),
                runtimeClock.elapsedMillis(), runtimeClock.isPaused(), runtimeClock.speedMultiplier(),
                season.map(SeasonState::seasonId).orElse(null), season.map(SeasonState::displayName).orElse(null),
                nations, points, edges, armies, armyOrders, armyOperationQueues, wallets, resources, localResources, generals,
                facilities, facilityConstructions, researchStates, diplomaticRelations, vassalRelations, independenceWars,
                fameScores, chronicleEntries, siegeState);
    }
}
