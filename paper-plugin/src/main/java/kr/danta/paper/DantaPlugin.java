package kr.danta.paper;

import kr.danta.core.facility.FacilityService;
import kr.danta.core.diplomacy.DiplomacyService;
import kr.danta.core.diplomacy.DiplomaticStatus;
import kr.danta.core.diplomacy.WarService;
import kr.danta.core.diplomacy.WarSide;
import kr.danta.core.diplomacy.DiplomaticAccessService;
import kr.danta.core.diplomacy.AccessRight;
import kr.danta.core.facility.FacilityConstructionService;
import kr.danta.paper.facility.FacilityAppearanceService;
import kr.danta.paper.facility.FacilityAppearanceListener;
import kr.danta.core.economy.SupplyConnectivityService;
import kr.danta.core.economy.ArmySupplyService;
import kr.danta.core.economy.AdministrativeCapacityService;
import kr.danta.core.economy.StrategicPointProductionService;
import kr.danta.core.economy.EconomyTickService;
import kr.danta.core.economy.StrategicResource;
import kr.danta.core.economy.StrategicResourceStockpile;
import kr.danta.core.economy.EconomyTransferService;
import kr.danta.core.economy.PersonalWallet;
import kr.danta.core.combat.CombatLossPolicy;
import kr.danta.core.combat.CombatReport;
import kr.danta.core.combat.CombatReportFormatter;
import kr.danta.core.combat.CombatResolver;
import kr.danta.core.combat.CombatSideInput;
import kr.danta.core.combat.TroopType;
import kr.danta.core.DantaCore;
import kr.danta.core.general.GeneralAcquisitionService;
import kr.danta.core.general.GeneralCatalogLoader;
import kr.danta.core.general.GeneralDefinition;
import kr.danta.core.general.GeneralState;
import kr.danta.core.general.PointGeneralAssignmentService;
import kr.danta.core.general.ArmyCommanderService;
import kr.danta.core.army.ArmyState;
import kr.danta.core.army.ArmyStatus;
import kr.danta.core.army.ExpeditionSupplyLevel;
import kr.danta.core.army.ExpeditionSupplyService;
import kr.danta.core.army.ArmyOrder;
import kr.danta.core.army.ArmyOrderService;
import kr.danta.core.army.ArmyMovementTime;
import kr.danta.core.army.ArmyMovementTimeService;
import kr.danta.core.army.ArmyOperationQueue;
import kr.danta.core.army.ArmyOperationQueueService;
import kr.danta.core.army.ArmyAdvanceDecision;
import kr.danta.core.army.ArmyAdvanceStopPolicy;
import kr.danta.core.event.DomainEventBus;
import kr.danta.core.nation.NationState;
import kr.danta.core.nation.NationStatus;
import kr.danta.core.persistence.AsyncKeyValueRepository;
import kr.danta.core.runtime.RuntimeClockRepository;
import kr.danta.core.runtime.RuntimeClockService;
import kr.danta.core.runtime.RuntimeClockState;
import kr.danta.core.runtime.RuntimeScheduledTask;
import kr.danta.core.runtime.RuntimeScheduler;
import kr.danta.core.runtime.RuntimeTaskExecution;
import kr.danta.core.season.SeasonPhaseService;
import kr.danta.core.season.SeasonAction;
import kr.danta.core.season.SeasonActionGate;
import kr.danta.core.score.FameScoreService;
import kr.danta.core.score.HegemonyScoreService;
import kr.danta.core.score.NationRankingService;
import kr.danta.core.chronicle.ChronicleService;
import kr.danta.core.chronicle.ChronicleEventType;
import kr.danta.core.territory.event.StrategicPointOwnershipChangedEvent;
import kr.danta.core.research.ResearchDefinition;
import kr.danta.core.research.ResearchDefinitionLoader;
import kr.danta.core.research.ResearchField;
import kr.danta.core.research.ResearchService;
import kr.danta.core.research.ResearchTier;
import kr.danta.core.snapshot.ArmyOrderSnapshot;
import kr.danta.core.snapshot.ArmyOperationQueueSnapshot;
import kr.danta.core.npc.NpcNationService;
import kr.danta.core.npc.NpcPoliticalService;
import kr.danta.core.nation.VassalService;
import kr.danta.core.nation.VassalPolicyService;
import kr.danta.core.nation.IndependenceWarService;
import kr.danta.core.nation.IndependenceSupportService;
import kr.danta.core.state.GameState;
import kr.danta.core.territory.BattlefieldTag;
import kr.danta.core.territory.PointPosition;
import kr.danta.core.territory.StrategicEdge;
import kr.danta.core.territory.StrategicPoint;
import kr.danta.core.territory.StrategicPointType;
import kr.danta.core.territory.TerritoryService;
import kr.danta.core.territory.event.StrategicPointOwnershipChangedEvent;
import kr.danta.paper.map.DevMapDefinition;
import kr.danta.paper.map.DevMapLoader;
import kr.danta.paper.map.DevMapService;
import kr.danta.paper.map.MapStructurePlacer;
import kr.danta.paper.persistence.DatabaseConfig;
import kr.danta.paper.persistence.DatabaseConfigLoader;
import kr.danta.paper.persistence.DatabaseHealth;
import kr.danta.paper.persistence.PostgresDatabaseService;
import kr.danta.paper.persistence.PostgresKeyValueRepository;
import kr.danta.paper.persistence.SnapshotService;
import kr.danta.paper.runtime.PropertiesRuntimeClockRepository;
import kr.danta.paper.ui.NationGuiController;
import kr.danta.paper.ui.ArmyGuiController;
import kr.danta.paper.ui.MapGuiController;
import kr.danta.paper.ui.UiText;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionException;

public final class DantaPlugin extends JavaPlugin implements CommandExecutor {
    private RuntimeClockService runtimeClock;
    private RuntimeClockRepository runtimeRepository;
    private GameState gameState;
    private DomainEventBus eventBus;
    private RuntimeScheduler runtimeScheduler;
    private SeasonPhaseService seasonPhaseService;
    private SeasonActionGate seasonActionGate;
    private FameScoreService fameScoreService;
    private HegemonyScoreService hegemonyScoreService;
    private NationRankingService nationRankingService;
    private ChronicleService chronicleService;
    private EconomyTickService economyTickService;
    private StrategicPointProductionService strategicPointProductionService;
    private ArmySupplyService armySupplyService;
    private ExpeditionSupplyService expeditionSupplyService;
    private GeneralAcquisitionService generalAcquisitionService;
    private PointGeneralAssignmentService pointGeneralAssignmentService;
    private ArmyCommanderService armyCommanderService;
    private FacilityService facilityService;
    private FacilityConstructionService facilityConstructionService;
    private FacilityAppearanceService facilityAppearanceService;
    private ResearchService researchService;
    private DiplomacyService diplomacyService;
    private WarService warService;
    private DiplomaticAccessService diplomaticAccessService;
    private NpcNationService npcNationService;
    private NpcPoliticalService npcPoliticalService;
    private VassalService vassalService;
    private VassalPolicyService vassalPolicyService;
    private IndependenceWarService independenceWarService;
    private IndependenceSupportService independenceSupportService;
    private java.util.Map<String, GeneralDefinition> generalCatalog = java.util.Map.of();
    private long economyTicksProcessed;
    private TerritoryService territoryService;
    private ArmyOrderService armyOrderService;
    private ArmyMovementTimeService armyMovementTimeService;
    private ArmyOperationQueueService armyOperationQueueService;
    private ArmyAdvanceStopPolicy armyAdvanceStopPolicy;
    private final java.util.Map<String, ArmyOperationQueue> armyOperationQueues = new java.util.LinkedHashMap<>();
    private NationGuiController nationGuiController;
    private MapGuiController mapGuiController;
    private ArmyGuiController armyGuiController;
    private DevMapDefinition devMapDefinition;
    private DevMapService devMapService;
    private MapStructurePlacer mapStructurePlacer;
    private BukkitTask runtimeSchedulerPump;

    private PostgresDatabaseService databaseService;
    private AsyncKeyValueRepository devRepository;
    private SnapshotService snapshotService;
    private final java.util.Map<String, ArmyOrderSnapshot> pendingMovementSnapshots = new java.util.LinkedHashMap<>();
    private BukkitTask snapshotTask;

    @Override public void onEnable() {
        gameState = new GameState();
        eventBus = new DomainEventBus();
        territoryService = new TerritoryService(gameState, eventBus);
        armyOrderService = new ArmyOrderService(gameState);
        armyMovementTimeService = new ArmyMovementTimeService(gameState);
        armyOperationQueueService = new ArmyOperationQueueService(gameState);
        armyAdvanceStopPolicy = new ArmyAdvanceStopPolicy(gameState);
        nationGuiController = new NationGuiController(gameState);
        getServer().getPluginManager().registerEvents(nationGuiController, this);
        mapGuiController = new MapGuiController(gameState);
        getServer().getPluginManager().registerEvents(mapGuiController, this);
        armyGuiController = new ArmyGuiController(gameState, armyOperationQueues);
        getServer().getPluginManager().registerEvents(armyGuiController, this);
        initializeDevMap();
        eventBus.subscribe(StrategicPointOwnershipChangedEvent.class, event -> {
            getLogger().info("[Territory] " + event.pointId() + ": "
                    + event.previousOwner().orElse("none") + " -> " + event.newOwner().orElse("none")
                    + " (" + event.reason() + ")");
            flushStrategicPointState("ownership-change:" + event.pointId());
        });
        runtimeClock = new RuntimeClockService();
        Path runtimeFile = getDataFolder().toPath().resolve("runtime.properties");
        runtimeRepository = new PropertiesRuntimeClockRepository(runtimeFile);
        restoreRuntime();
        runtimeClock.start();
        seasonPhaseService = new SeasonPhaseService(runtimeClock::elapsedMillis);
        seasonActionGate = new SeasonActionGate(runtimeClock::elapsedMillis);
        economyTickService = new EconomyTickService(runtimeClock.elapsedMillis());
        strategicPointProductionService = null; // initialized after vassal policy wiring
        armySupplyService = new ArmySupplyService(gameState);
        expeditionSupplyService = new ExpeditionSupplyService(gameState);
        generalAcquisitionService = new GeneralAcquisitionService(gameState);
        diplomacyService = new DiplomacyService(gameState);
        npcNationService = new NpcNationService(gameState);
        npcPoliticalService = new NpcPoliticalService(gameState, npcNationService, diplomacyService, territoryService);
        vassalService = new VassalService(gameState);
        vassalPolicyService = new VassalPolicyService(gameState, vassalService);
        fameScoreService = new FameScoreService(gameState);
        HegemonyScoreService.HegemonyScorePolicy provisionalScorePolicy = type -> 0L;
        hegemonyScoreService = new HegemonyScoreService(gameState, provisionalScorePolicy, vassalService, npcNationService);
        nationRankingService = new NationRankingService(gameState, fameScoreService, hegemonyScoreService);
        chronicleService = new ChronicleService();
        eventBus.subscribe(StrategicPointOwnershipChangedEvent.class, event -> chronicleService.record(
                runtimeClock.elapsedMillis(), ChronicleEventType.TERRITORY_CHANGE,
                "거점 " + event.pointId() + " 소유권 변경: "
                        + (event.previousOwnerNationId() == null ? "중립" : event.previousOwnerNationId()) + " → "
                        + (event.newOwnerNationId() == null ? "중립" : event.newOwnerNationId())));
        independenceWarService = new IndependenceWarService(gameState, vassalService);
        independenceSupportService = new IndependenceSupportService(gameState, vassalService);
        strategicPointProductionService = new StrategicPointProductionService(gameState, vassalPolicyService);
        warService = new WarService(gameState, diplomacyService, vassalPolicyService);
        diplomaticAccessService = new DiplomaticAccessService(gameState, diplomacyService, vassalPolicyService);
        pointGeneralAssignmentService = new PointGeneralAssignmentService(gameState);
        armyCommanderService = new ArmyCommanderService(gameState);
        loadGeneralCatalog();

        runtimeScheduler = new RuntimeScheduler(runtimeClock);
        runtimeScheduler.registerHandler("dev.echo", task ->
                getLogger().info("[RuntimeScheduler] dev.echo completed: "
                        + task.payload().getOrDefault("message", "(no message)")
                        + " [id=" + task.id() + "]"));
        runtimeScheduler.registerHandler("army.move.arrive", task ->
                completeArmyMovement(task.payload().get("armyId"), task.payload().get("orderId")));
        // DEV-088 development sample tree. This is intentionally provisional test content, not the final season tree.
        java.util.Map<String, ResearchDefinition> devResearchDefinitions;
        try (var input = getResource("research/dev088-sample-research.yml")) {
            if (input == null) throw new IllegalStateException("DEV-088 sample research resource is missing");
            devResearchDefinitions = new ResearchDefinitionLoader().load(input);
        } catch (java.io.IOException ex) {
            throw new IllegalStateException("DEV-088 sample research resource could not be closed", ex);
        }
        researchService = new ResearchService(runtimeScheduler, devResearchDefinitions);
        runtimeScheduler.registerHandler(ResearchService.TASK_TYPE, task -> {
            researchService.complete(UUID.fromString(task.payload().get("entryId")), task.payload().get("nationId"));
            flushResearchState("research-complete:" + task.payload().get("nationId") + ":" + task.payload().get("entryId"));
        });
        facilityService = new FacilityService(gameState);
        facilityConstructionService = new FacilityConstructionService(gameState, facilityService, runtimeScheduler);
        facilityAppearanceService = new FacilityAppearanceService(getServer(), gameState, facilityService);
        getServer().getPluginManager().registerEvents(
                new FacilityAppearanceListener(facilityAppearanceService, getLogger()), this);
        runtimeScheduler.registerHandler(FacilityConstructionService.TASK_TYPE, task -> {
            facilityConstructionService.complete(UUID.fromString(task.payload().get("constructionId")));
            String pointId = task.payload().get("pointId");
            String facilityId = task.payload().get("facilityId");
            facilityAppearanceService.queue(pointId, facilityId);
            var visualResult = facilityAppearanceService.sync(pointId, facilityId);
            if (visualResult != FacilityAppearanceService.SyncResult.SYNCED) {
                getLogger().info("[DEV-082] facility visual pending: " + pointId + "/" + facilityId + " (" + visualResult + ")");
            }
            flushFacilityState("facility-construction-complete:" + task.payload().get("pointId")
                    + ":" + task.payload().get("facilityId"));
        });
        runtimeSchedulerPump = getServer().getScheduler().runTaskTimer(
                this, this::pumpRuntimeScheduler, 1L, 1L);

        initializeDatabase();

        PluginCommand danta = getCommand("danta");
        if (danta == null) {
            getLogger().severe("Command /danta is missing from plugin.yml; disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        danta.setExecutor(this);

        PluginCommand nationGui = getCommand("nationgui");
        if (nationGui == null) {
            getLogger().warning("Command /nationgui (alias /국가) is missing from plugin.yml.");
        } else {
            nationGui.setExecutor(this);
        }
        PluginCommand mapGui = getCommand("mapgui");
        if (mapGui == null) {
            getLogger().warning("Command /mapgui (alias /지도) is missing from plugin.yml.");
        } else {
            mapGui.setExecutor(this);
        }
        getLogger().info("Danta Server DEV enabled. Core version: " + DantaCore.VERSION
                + ", runtime=" + formatRuntime(runtimeClock.elapsedMillis()));
    }

    @Override public void onDisable() {
        if (runtimeSchedulerPump != null) runtimeSchedulerPump.cancel();
        if (snapshotTask != null) snapshotTask.cancel();
        if (runtimeClock != null) runtimeClock.stop();
        persistRuntime();
        if (snapshotService != null && databaseService != null && databaseService.health().status().name().equals("READY")) snapshotService.flushOnShutdown();
        if (databaseService != null) databaseService.close();
        getLogger().info("Danta Server DEV disabled. Runtime: "
                + formatRuntime(runtimeClock == null ? 0L : runtimeClock.elapsedMillis()));
    }

    private void initializeDatabase() {
        Path databaseFile = getDataFolder().toPath().resolve("database.properties");
        try {
            DatabaseConfig config = DatabaseConfigLoader.loadOrCreate(databaseFile);
            databaseService = new PostgresDatabaseService(config, getLogger());
            devRepository = new PostgresKeyValueRepository(databaseService);
            snapshotService = new SnapshotService(devRepository, runtimeClock, gameState, getLogger());
            snapshotService.bindFacilities(facilityService, facilityConstructionService);
            snapshotService.bindResearch(researchService);
            snapshotService.bindDiplomacy(diplomacyService);
            snapshotService.bindVassals(vassalService);
            snapshotService.bindIndependenceWars(independenceWarService);
            snapshotService.bindChronicle(chronicleService);
            if (config.enabled()) {
                databaseService.initializeAsync().thenAccept(ready -> {
                    if (!ready) {
                        getLogger().warning("DEV-016: PostgreSQL unavailable. Game remains online; use /danta db status.");
                        return;
                    }
                    snapshotService.loadAsync().whenComplete((snapshot, error) -> runSync(() -> {
                        if (error != null) {
                            getLogger().warning("DEV-017 snapshot recovery failed; local runtime state remains active: " + rootMessage(error));
                        } else if (snapshot.isPresent()) {
                            snapshotService.apply(snapshot.get());
                            facilityAppearanceService.queueAllInstalled();
                            for (var point : gameState.strategicPoints()) {
                                for (var facility : facilityService.facilities(point.pointId())) {
                                    facilityAppearanceService.sync(point.pointId(), facility.facilityId());
                                }
                            }
                            restoreArmyMovements();
                            persistRuntime();
                            getLogger().info("DEV-017 snapshot recovered. Runtime=" + formatRuntime(runtimeClock.elapsedMillis()));
                        } else {
                            getLogger().info("DEV-017: no database snapshot yet; local runtime state remains active.");
                        }
                    }));
                    runSync(this::startSnapshotTask);
                });
            } else {
                getLogger().info("DEV-016: PostgreSQL disabled. Edit plugins/DantaServer/database.properties when ready.");
            }
        } catch (Exception ex) {
            getLogger().severe("DEV-016 database configuration failed; persistence disabled: " + ex.getMessage());
            databaseService = null;
            devRepository = null;
        }
    }

    private void restoreRuntime() {
        try {
            runtimeRepository.load().ifPresent(state -> {
                runtimeClock.setElapsedMillis(state.elapsedMillis());
                runtimeClock.setSpeedMultiplier(state.speedMultiplier());
                if (state.paused()) runtimeClock.pause();
            });
        } catch (Exception ex) {
            getLogger().severe("Failed to restore runtime; starting from zero: " + ex.getMessage());
        }
    }

    private void persistRuntime() {
        if (runtimeClock == null || runtimeRepository == null) return;
        try {
            runtimeRepository.save(new RuntimeClockState(
                    runtimeClock.elapsedMillis(), runtimeClock.isPaused(), runtimeClock.speedMultiplier()));
        } catch (IOException ex) {
            getLogger().severe("Failed to persist runtime: " + ex.getMessage());
        }
    }

    @Override public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("nationgui")) return handleNationGuiCommand(sender, args);
        if (command.getName().equalsIgnoreCase("mapgui")) return handleMapGuiCommand(sender);
        if (!command.getName().equalsIgnoreCase("danta")) return false;
        if (args.length > 0 && args[0].equalsIgnoreCase("runtime")) return handleRuntime(sender, args);
        if (args.length > 0 && args[0].equalsIgnoreCase("season")) return handleSeason(sender, args);
        if (args.length > 0 && args[0].equalsIgnoreCase("ranking")) return handleRanking(sender);
        if (args.length > 0 && args[0].equalsIgnoreCase("chronicle")) return handleChronicle(sender);
        if (args.length > 0 && args[0].equalsIgnoreCase("db")) return handleDatabase(sender, args);
        if (args.length > 0 && args[0].equalsIgnoreCase("snapshot")) return handleSnapshot(sender, args);
        if (args.length > 0 && args[0].equalsIgnoreCase("nation")) return handleNation(sender, args);
        if (args.length > 0 && args[0].equalsIgnoreCase("point")) return handleStrategicPoint(sender, args);
        if (args.length > 0 && args[0].equalsIgnoreCase("edge")) return handleStrategicEdge(sender, args);
        if (args.length > 0 && args[0].equalsIgnoreCase("army")) return handleArmy(sender, args);
        if (args.length > 0 && args[0].equalsIgnoreCase("combat")) return handleCombat(sender, args);
        if (args.length > 0 && args[0].equalsIgnoreCase("economy")) return handleEconomy(sender, args);
        if (args.length > 0 && args[0].equalsIgnoreCase("resource")) return handleResource(sender, args);
        if (args.length > 0 && args[0].equalsIgnoreCase("economy-tick")) return handleEconomyTick(sender);
        if (args.length > 0 && args[0].equalsIgnoreCase("devmap")) return handleDevMap(sender, args);
        if (args.length > 0 && args[0].equalsIgnoreCase("general")) return handleGeneral(sender, args);
        if (args.length > 0 && args[0].equalsIgnoreCase("facility")) return handleFacility(sender, args);
        if (args.length > 0 && args[0].equalsIgnoreCase("research")) return handleResearch(sender, args);
        if (args.length > 0 && args[0].equalsIgnoreCase("diplomacy")) return handleDiplomacy(sender, args);

        sender.sendMessage("§6[단타 서버 개발 정보]");
        sender.sendMessage("§f플러그인 버전: §e" + getPluginMeta().getVersion());
        sender.sendMessage("§f코어 버전: §e" + DantaCore.VERSION);
        sender.sendMessage("§fMinecraft/Paper 대상: §e26.2 / 빌드 123");
        sender.sendMessage("§fJava 대상: §e25");
        sender.sendMessage("§f서버 가동 시간: §e" + formatRuntime(runtimeClock.elapsedMillis())
                + (runtimeClock.isPaused() ? " §c[일시정지]" : "") + " §7x" + runtimeClock.speedMultiplier());
        sender.sendMessage("§7예약 작업 수: " + runtimeScheduler.size());
        if (databaseService == null) {
            sender.sendMessage("§7데이터베이스: §c설정 오류");
        } else {
            sender.sendMessage("§7데이터베이스: §e" + UiText.databaseStatus(databaseService.health().status()));
        }
        return true;
    }



    private void loadGeneralCatalog() {
        try (var input = getResource("generals/initial-generals.yml")) {
            if (input == null) throw new IllegalStateException("initial-generals.yml resource missing");
            var definitions = new GeneralCatalogLoader().load(input);
            java.util.LinkedHashMap<String, GeneralDefinition> byId = new java.util.LinkedHashMap<>();
            for (GeneralDefinition definition : definitions) byId.put(definition.id(), definition);
            generalCatalog = java.util.Map.copyOf(byId);
            getLogger().info("[General] development catalog loaded: " + generalCatalog.size());
        } catch (Exception ex) {
            generalCatalog = java.util.Map.of();
            getLogger().severe("[General] development catalog load failed: " + rootMessage(ex));
        }
    }

    private boolean handleGeneral(CommandSender sender, String[] args) {
        if (!sender.hasPermission("danta.admin.general")) {
            sender.sendMessage("§c장수 관리 권한이 없습니다.");
            return true;
        }
        String sub = args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : "list";
        try {
            switch (sub) {
                case "catalog" -> {
                    sender.sendMessage("§6[개발용 장수 후보 목록] §7총 " + generalCatalog.size() + "명");
                    for (GeneralDefinition definition : generalCatalog.values()) {
                        sender.sendMessage("§e" + definition.id() + " §f" + definition.displayName()
                                + " §7등급=" + definition.grade() + ", 레벨=" + definition.level());
                    }
                }
                case "acquire" -> {
                    requireArgs(args, 4, "/danta general acquire <장수-id> <국가-id>");
                    GeneralDefinition definition = Optional.ofNullable(generalCatalog.get(args[2]))
                            .orElseThrow(() -> new IllegalArgumentException("catalog general not found: " + args[2]));
                    GeneralState general = generalAcquisitionService.acquire(definition, args[3]);
                    flushGeneralState("general-acquire:" + general.generalId());
                    sender.sendMessage("§a개발 검증용 장수를 등용했습니다: §e" + definition.displayName()
                            + " §7(" + general.generalId() + "), 소유국=" + general.ownerNationId());
                }
                case "list" -> {
                    sender.sendMessage("§6[소유 장수 목록] §7총 " + gameState.generals().size() + "명");
                    for (GeneralState general : gameState.generals()) sendGeneral(sender, general);
                }
                case "show" -> {
                    requireArgs(args, 3, "/danta general show <장수-id>");
                    sendGeneral(sender, requireGeneral(args[2]));
                }
                case "point" -> {
                    requireArgs(args, 4, "/danta general point <장수-id> <거점-id>");
                    pointGeneralAssignmentService.assign(args[2], args[3]);
                    flushGeneralState("general-point:" + args[2]);
                    sender.sendMessage("§a장수를 거점에 배치했습니다: §e" + args[2] + " §7거점=" + args[3]);
                }
                case "point-move" -> {
                    requireArgs(args, 4, "/danta general point-move <장수-id> <거점-id>");
                    pointGeneralAssignmentService.move(args[2], args[3]);
                    flushGeneralState("general-point-move:" + args[2]);
                    sender.sendMessage("§a장수의 거점 배치를 변경했습니다: §e" + args[2] + " §7거점=" + args[3]);
                }
                case "point-clear" -> {
                    requireArgs(args, 3, "/danta general point-clear <장수-id>");
                    pointGeneralAssignmentService.unassign(args[2]);
                    flushGeneralState("general-point-clear:" + args[2]);
                    sender.sendMessage("§a장수의 거점 배치를 해제했습니다: §e" + args[2]);
                }
                case "army" -> {
                    requireArgs(args, 4, "/danta general army <장수-id> <군단-id>");
                    armyCommanderService.assign(args[2], args[3]);
                    flushGeneralState("general-army:" + args[2]);
                    sender.sendMessage("§a장수를 군단 지휘관으로 임명했습니다: §e" + args[2] + " §7군단=" + args[3]);
                }
                case "army-clear" -> {
                    requireArgs(args, 3, "/danta general army-clear <장수-id>");
                    armyCommanderService.unassign(args[2]);
                    flushGeneralState("general-army-clear:" + args[2]);
                    sender.sendMessage("§a장수의 군단 지휘를 해제했습니다: §e" + args[2]);
                }
                default -> sender.sendMessage("§e/danta general <catalog|acquire|list|show|point|point-move|point-clear|army|army-clear>");
            }
        } catch (RuntimeException ex) {
            sendCommandError(sender, "장수", ex);
        }
        return true;
    }

    private GeneralState requireGeneral(String generalId) {
        return gameState.general(generalId)
                .orElseThrow(() -> new IllegalArgumentException("general not found: " + generalId));
    }

    private void sendGeneral(CommandSender sender, GeneralState general) {
        sender.sendMessage("§6[장수] §e" + general.generalId());
        sender.sendMessage("§f소유국: §e" + general.ownerNationId() + " §7등급=" + general.grade() + ", 레벨=" + general.level());
        sender.sendMessage("§f능력치: §e통솔 " + general.stats().command() + " / 무력 " + general.stats().martial()
                + " / 지략 " + general.stats().strategy() + " / 병참 " + general.stats().logistics());
        sender.sendMessage("§f군단 지휘: §e" + gameState.commandedArmyId(general.generalId()).orElse("없음")
                + " §f거점 배치: §e" + gameState.assignedPointId(general.generalId()).orElse("없음"));
        sender.sendMessage("§f건강: §e" + general.healthStatus().name()
                + " §f포로: §e" + (general.isCaptive() ? "예" : "아니요"));
    }

    private void flushGeneralState(String reason) {
        if (snapshotService == null || databaseService == null) return;
        if (!databaseService.health().status().name().equals("READY")) return;
        snapshotService.flushImportantAsync(reason).whenComplete((ignored, error) -> {
            if (error != null) getLogger().warning("DEV-076F general snapshot flush failed: " + rootMessage(error));
        });
    }

    private void initializeDevMap() {
        try {
            devMapDefinition = new DevMapLoader(this).loadDefault();
            devMapService = new DevMapService(gameState);
            mapStructurePlacer = new MapStructurePlacer(this);
            getLogger().info("DEV-MAP-002 map definition ready: " + devMapDefinition.mapId()
                    + " points=" + devMapDefinition.points().size()
                    + ", edges=" + devMapDefinition.edges().size());
        } catch (RuntimeException ex) {
            devMapDefinition = null;
            devMapService = null;
            mapStructurePlacer = null;
            getLogger().severe("DEV-MAP-002 map definition failed: " + ex.getMessage());
        }
    }

    private boolean handleDevMap(CommandSender sender, String[] args) {
        if (!sender.hasPermission("danta.admin.map")) {
            sender.sendMessage("§c개발 지도 관리 권한이 없습니다.");
            return true;
        }
        if (devMapDefinition == null || devMapService == null || mapStructurePlacer == null) {
            sender.sendMessage("§c개발 지도를 사용할 수 없습니다. 서버 콘솔을 확인해 주세요.");
            return true;
        }

        String sub = args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : "status";
        try {
            switch (sub) {
                case "status" -> {
                    sender.sendMessage("§6[단타 개발 지도] §e" + devMapDefinition.mapId());
                    sender.sendMessage("§f월드: §e" + devMapDefinition.worldName());
                    sender.sendMessage("§f설정 데이터: §e거점 " + devMapDefinition.points().size()
                            + "개 / 간선 " + devMapDefinition.edges().size() + "개");
                    sender.sendMessage("§f현재 게임 상태: §e거점 " + gameState.strategicPoints().size()
                            + "개 / 간선 " + gameState.strategicEdges().size() + "개");
                    sender.sendMessage("§7설정 파일: plugins/DantaServer/maps/dev-test-map.yml");
                }
                case "load", "import" -> {
                    DevMapService.ImportResult result = devMapService.importDefinition(devMapDefinition);
                    flushDevMapState("devmap-import");
                    sendDevMapImportResult(sender, result);
                }
                case "place" -> {
                    MapStructurePlacer.PlacementResult result = mapStructurePlacer.placeAll(devMapDefinition);
                    sender.sendMessage("§a개발 지도 표식 배치 완료: §e" + result.placed()
                            + "개 §7실패=" + result.failed() + "개");
                    for (String failure : result.failures()) sender.sendMessage("§c- 배치 실패(콘솔 확인)");
                }
                case "apply" -> {
                    DevMapService.ImportResult importResult = devMapService.importDefinition(devMapDefinition);
                    flushDevMapState("devmap-apply");
                    sendDevMapImportResult(sender, importResult);
                    MapStructurePlacer.PlacementResult placement = mapStructurePlacer.placeAll(devMapDefinition);
                    sender.sendMessage("§a개발 지도 표식 배치 완료: §e" + placement.placed()
                            + "개 §7실패=" + placement.failed() + "개");
                    for (String failure : placement.failures()) sender.sendMessage("§c- 배치 실패(콘솔 확인)");
                }
                default -> sender.sendMessage("§e/danta devmap <status|load|place|apply>");
            }
        } catch (RuntimeException ex) {
            sendCommandError(sender, "개발 지도", ex);
        }
        return true;
    }

    private void sendDevMapImportResult(CommandSender sender, DevMapService.ImportResult result) {
        sender.sendMessage("§a개발 지도 논리 데이터를 불러왔습니다.");
        sender.sendMessage("§f국가: §e신규 " + result.createdNations() + "개 §7기존=" + result.existingNations() + "개");
        sender.sendMessage("§f거점: §e신규 " + result.createdPoints() + "개 §7기존=" + result.existingPoints() + "개");
        sender.sendMessage("§f간선: §e신규 " + result.createdEdges() + "개 §7기존=" + result.existingEdges() + "개");
    }

    private void flushDevMapState(String reason) {
        if (snapshotService == null || databaseService == null) return;
        if (!databaseService.health().status().name().equals("READY")) return;
        snapshotService.flushImportantAsync(reason).whenComplete((ignored, error) -> {
            if (error != null) getLogger().warning("DEV-MAP-002 snapshot flush failed: " + rootMessage(error));
        });
    }


    private boolean handleMapGuiCommand(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c지도 GUI는 플레이어만 열 수 있습니다.");
            return true;
        }
        mapGuiController.open(player);
        return true;
    }


    private boolean handleNationGuiCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c국가 화면은 플레이어만 열 수 있습니다.");
            return true;
        }
        String nationId = args.length >= 1 ? args[0] : null;
        try {
            nationGuiController.open(player, nationId);
        } catch (RuntimeException ex) {
            sendCommandError(sender, "국가 화면", ex);
        }
        return true;
    }


    private boolean handleStrategicEdge(CommandSender sender, String[] args) {
        if (!sender.hasPermission("danta.admin.edge")) {
            sender.sendMessage("§c전략 간선 관리 권한이 없습니다.");
            return true;
        }
        String sub = args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : "list";
        try {
            switch (sub) {
                case "create" -> {
                    requireArgs(args, 7, "/danta edge create <간선-id> <거점-a> <거점-b> <이동-초> <특성[,특성...]|none>");
                    String id = args[2];
                    String pointA = args[3];
                    String pointB = args[4];
                    requireStrategicPoint(pointA);
                    requireStrategicPoint(pointB);
                    long travelSeconds = Long.parseLong(args[5]);
                    if (travelSeconds <= 0 || travelSeconds > 86_400L) {
                        throw new IllegalArgumentException("travel-seconds must be 1..86400");
                    }
                    StrategicEdge edge = new StrategicEdge(id, pointA, pointB,
                            Math.multiplyExact(travelSeconds, 1000L), parseBattlefieldTags(args[6]));
                    gameState.addStrategicEdge(edge);
                    flushStrategicEdgeState("edge-create:" + id);
                    sender.sendMessage("§a전략 간선을 생성했습니다: §e" + id + " §f(" + pointA + " <-> " + pointB + ")");
                }
                case "list" -> {
                    sender.sendMessage("§6[전략 간선 목록] §7총 " + gameState.strategicEdges().size() + "개");
                    for (StrategicEdge edge : gameState.strategicEdges()) {
                        sender.sendMessage("§e" + edge.edgeId() + " §f" + edge.pointAId() + " <-> " + edge.pointBId()
                                + " §7이동시간=" + formatTravel(edge.baseTravelMillis()) + ", 전장=" + formatBattlefieldTags(edge));
                    }
                }
                case "show" -> {
                    requireArgs(args, 3, "/danta edge show <간선-id>");
                    sendStrategicEdge(sender, requireStrategicEdge(args[2]));
                }
                case "travel" -> {
                    requireArgs(args, 4, "/danta edge travel <간선-id> <초>");
                    StrategicEdge edge = requireStrategicEdge(args[2]);
                    long seconds = Long.parseLong(args[3]);
                    if (seconds <= 0 || seconds > 86_400L) throw new IllegalArgumentException("seconds must be 1..86400");
                    edge.setBaseTravelMillis(Math.multiplyExact(seconds, 1000L));
                    flushStrategicEdgeState("edge-travel:" + edge.edgeId());
                    sender.sendMessage("§a이동시간을 변경했습니다: §e" + formatTravel(edge.baseTravelMillis()));
                }
                case "tags" -> {
                    requireArgs(args, 4, "/danta edge tags <간선-id> <특성[,특성...]|none>");
                    StrategicEdge edge = requireStrategicEdge(args[2]);
                    edge.setBattlefieldTags(parseBattlefieldTags(args[3]));
                    flushStrategicEdgeState("edge-tags:" + edge.edgeId());
                    sender.sendMessage("§a전장 특성을 변경했습니다: §e" + formatBattlefieldTags(edge));
                }
                case "neighbors" -> {
                    requireArgs(args, 3, "/danta edge neighbors <거점-id>");
                    StrategicPoint point = requireStrategicPoint(args[2]);
                    var edges = gameState.edgesForPoint(point.pointId());
                    sender.sendMessage("§6[인접 거점] §e" + point.pointId() + " §7총 " + edges.size() + "개");
                    for (StrategicEdge edge : edges) {
                        sender.sendMessage("§f- §e" + edge.otherPoint(point.pointId()) + " §7간선=" + edge.edgeId()
                                + ", 이동시간=" + formatTravel(edge.baseTravelMillis()) + ", 전장=" + formatBattlefieldTags(edge));
                    }
                }
                default -> sender.sendMessage("§e/danta edge <create|list|show|travel|tags|neighbors>");
            }
        } catch (RuntimeException ex) {
            sendCommandError(sender, "전략 간선", ex);
        }
        return true;
    }

    private StrategicEdge requireStrategicEdge(String edgeId) {
        return gameState.strategicEdge(edgeId)
                .orElseThrow(() -> new IllegalArgumentException("strategic edge not found: " + edgeId));
    }

    private void sendStrategicEdge(CommandSender sender, StrategicEdge edge) {
        sender.sendMessage("§6[전략 간선] §e" + edge.edgeId());
        sender.sendMessage("§f연결 거점: §e" + edge.pointAId() + " §f<-> §e" + edge.pointBId());
        sender.sendMessage("§f기본 이동시간: §e" + formatTravel(edge.baseTravelMillis()));
        sender.sendMessage("§f전장 특성: §e" + formatBattlefieldTags(edge));
    }

    private java.util.Set<BattlefieldTag> parseBattlefieldTags(String raw) {
        if (raw == null || raw.isBlank() || raw.equalsIgnoreCase("none") || raw.equals("-")) {
            return java.util.Set.of();
        }
        java.util.LinkedHashSet<BattlefieldTag> result = new java.util.LinkedHashSet<>();
        for (String token : raw.split(",")) {
            String normalized = token.trim();
            if (!normalized.isEmpty()) result.add(BattlefieldTag.valueOf(normalized.toUpperCase(Locale.ROOT)));
        }
        return java.util.Set.copyOf(result);
    }

    private String formatTravel(long millis) {
        long totalSeconds = Math.max(0L, millis / 1000L);
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        return minutes + "분 " + seconds + "초";
    }

    private String formatBattlefieldTags(StrategicEdge edge) {
        if (edge.battlefieldTags().isEmpty()) return "없음";
        return edge.battlefieldTags().stream()
                .map(UiText::battlefieldTag)
                .sorted()
                .reduce((left, right) -> left + ", " + right)
                .orElse("없음");
    }

    private void flushStrategicEdgeState(String reason) {
        if (snapshotService == null || databaseService == null) return;
        if (!databaseService.health().status().name().equals("READY")) return;
        snapshotService.flushImportantAsync(reason).whenComplete((ignored, error) -> {
            if (error != null) getLogger().warning("DEV-022 strategic edge snapshot flush failed: " + rootMessage(error));
        });
    }


    private boolean handleStrategicPoint(CommandSender sender, String[] args) {
        if (!sender.hasPermission("danta.admin.point")) {
            sender.sendMessage("§c전략 거점 관리 권한이 없습니다.");
            return true;
        }
        String sub = args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : "list";
        try {
            switch (sub) {
                case "create" -> {
                    requireArgs(args, 10, "/danta point create <거점-id> <종류> <월드> <x> <y> <z> <슬롯> <표시-이름>");
                    String id = args[2];
                    StrategicPointType type = StrategicPointType.valueOf(args[3].toUpperCase(Locale.ROOT));
                    String world = args[4];
                    int x = Integer.parseInt(args[5]);
                    int y = Integer.parseInt(args[6]);
                    int z = Integer.parseInt(args[7]);
                    int slots = Integer.parseInt(args[8]);
                    String name = String.join(" ", Arrays.copyOfRange(args, 9, args.length));
                    StrategicPoint point = new StrategicPoint(id, name, type, new PointPosition(world, x, y, z), slots);
                    gameState.addStrategicPoint(point);
                    flushStrategicPointState("point-create:" + id);
                    sender.sendMessage("§a전략 거점을 생성했습니다: §e" + id + " §f(" + name + ")");
                }
                case "list" -> {
                    sender.sendMessage("§6[전략 거점 목록] §7총 " + gameState.strategicPoints().size() + "개");
                    for (StrategicPoint point : gameState.strategicPoints()) {
                        PointPosition pos = point.position();
                        sender.sendMessage("§e" + point.pointId() + " §f" + point.displayName()
                                + " §7종류=" + UiText.strategicPointType(point.type()) + ", 소유국=" + point.ownerNationId().orElse("없음")
                                + ", 위치=" + pos.worldName() + ":" + pos.x() + "," + pos.y() + "," + pos.z());
                    }
                }
                case "show" -> {
                    requireArgs(args, 3, "/danta point show <거점-id>");
                    sendStrategicPoint(sender, requireStrategicPoint(args[2]));
                }
                case "owner" -> {
                    requireArgs(args, 4, "/danta point owner <거점-id> <국가-id|none>");
                    TerritoryService.OwnershipChangeResult result = territoryService.changeOwner(
                            args[2], args[3], "admin-command");
                    if (result.changed()) {
                        sender.sendMessage("§a소유국을 변경했습니다: §e"
                                + (result.previousOwnerNationId() == null ? "없음" : result.previousOwnerNationId())
                                + " §f-> §e"
                                + (result.newOwnerNationId() == null ? "없음" : result.newOwnerNationId()));
                    } else {
                        sender.sendMessage("§7소유국이 이미 동일합니다: §e"
                                + (result.newOwnerNationId() == null ? "없음" : result.newOwnerNationId()));
                    }
                }
                case "production" -> {
                    requireArgs(args, 5, "/danta point production <거점-id> <자원-key> <시간당-생산량>");
                    StrategicPoint point = requireStrategicPoint(args[2]);
                    point.setBaseProduction(args[3], Long.parseLong(args[4]));
                    flushStrategicPointState("point-production:" + point.pointId());
                    sender.sendMessage("§a시간당 생산량을 변경했습니다: §e" + args[3].toLowerCase(Locale.ROOT)
                            + "=" + point.baseProductionPerHour().getOrDefault(args[3].toLowerCase(Locale.ROOT), 0L) + "/시간");
                }
                case "slots" -> {
                    requireArgs(args, 4, "/danta point slots <거점-id> <0-16>");
                    StrategicPoint point = requireStrategicPoint(args[2]);
                    point.setFacilitySlots(Integer.parseInt(args[3]));
                    flushStrategicPointState("point-slots:" + point.pointId());
                    sender.sendMessage("§a시설 슬롯 수를 변경했습니다: §e" + point.facilitySlots());
                }
                default -> sender.sendMessage("§e/danta point <create|list|show|owner|production|slots>");
            }
        } catch (RuntimeException ex) {
            sendCommandError(sender, "전략 거점", ex);
        }
        return true;
    }

    private StrategicPoint requireStrategicPoint(String pointId) {
        return gameState.strategicPoint(pointId)
                .orElseThrow(() -> new IllegalArgumentException("strategic point not found: " + pointId));
    }

    private void sendStrategicPoint(CommandSender sender, StrategicPoint point) {
        PointPosition pos = point.position();
        sender.sendMessage("§6[전략 거점] §e" + point.pointId());
        sender.sendMessage("§f이름: §e" + point.displayName());
        sender.sendMessage("§f종류: §e" + UiText.strategicPointType(point.type()));
        sender.sendMessage("§f소유국: §e" + point.ownerNationId().orElse("없음"));
        sender.sendMessage("§f위치: §e" + pos.worldName() + " " + pos.x() + " " + pos.y() + " " + pos.z());
        sender.sendMessage("§f시설 슬롯: §e" + point.facilitySlots());
        sender.sendMessage("§f기본 시간당 생산량: §e" + point.baseProductionPerHour());
    }

    private void flushStrategicPointState(String reason) {
        if (snapshotService == null || databaseService == null) return;
        if (!databaseService.health().status().name().equals("READY")) return;
        snapshotService.flushImportantAsync(reason).whenComplete((ignored, error) -> {
            if (error != null) getLogger().warning("DEV-021 strategic point snapshot flush failed: " + rootMessage(error));
        });
    }

    private boolean handleEconomyTick(CommandSender sender) {
        if (!sender.hasPermission("danta.admin.nation")) {
            sender.sendMessage("§c경제 상태 확인 권한이 없습니다.");
            return true;
        }
        long now = runtimeClock.elapsedMillis();
        long next = economyTickService == null ? now : economyTickService.nextTickRuntimeMillis();
        sender.sendMessage("§6[경제 Tick]");
        sender.sendMessage("§f처리 누적: §e" + economyTicksProcessed + "회");
        sender.sendMessage("§f현재 서버시간: §e" + formatRuntime(now));
        sender.sendMessage("§f다음 Tick: §e" + formatRuntime(next));
        sender.sendMessage("§7경제 Tick은 서버 러닝타임 30분마다 발생합니다.");
        return true;
    }

    private boolean handleResource(CommandSender sender, String[] args) {
        if (!sender.hasPermission("danta.admin.nation")) {
            sender.sendMessage("§c전략자원 관리 권한이 없습니다.");
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage("§e사용법: /danta resource <show|set|available|local-show> <국가/거점-id> [자원] [수량]");
            return true;
        }
        try {
            String sub = args[1].toLowerCase(Locale.ROOT);
            if (sub.equals("show")) {
                StrategicResourceStockpile stock = gameState.getOrCreateStrategicResourceStockpile(args[2]);
                sender.sendMessage("§6[전략자원] §f" + args[2]);
                sender.sendMessage("§f식량: §e" + stock.amount(StrategicResource.FOOD));
                sender.sendMessage("§f목재: §e" + stock.amount(StrategicResource.WOOD));
                sender.sendMessage("§f철: §e" + stock.amount(StrategicResource.IRON));
                sender.sendMessage("§f희귀광물: §e" + stock.amount(StrategicResource.RARE_MINERAL));
                sender.sendMessage("§f마력석: §e" + stock.amount(StrategicResource.MANA_STONE));
                return true;
            }
            if (sub.equals("local-show")) {
                String pointId = args[2];
                var point = gameState.strategicPoint(pointId)
                        .orElseThrow(() -> new IllegalArgumentException("거점을 찾을 수 없습니다: " + pointId));
                var local = gameState.getOrCreateLocalResourceStockpile(pointId);
                String owner = point.ownerNationId().orElse(null);
                boolean connected = owner != null && new SupplyConnectivityService(gameState).isConnectedToCapital(owner, pointId);
                sender.sendMessage("§6[현지 비축] §f" + point.displayName() + " §7(" + pointId + ")");
                sender.sendMessage("§f수도 보급망: " + (connected ? "§a연결" : "§c고립"));
                for (StrategicResource resource : StrategicResource.values())
                    sender.sendMessage("§f" + resourceKorean(resource) + ": §e" + local.amount(resource));
                return true;
            }
            if (sub.equals("available")) {
                String nationId = args[2];
                if (!gameState.hasNation(nationId)) throw new IllegalArgumentException("국가를 찾을 수 없습니다: " + nationId);
                SupplyConnectivityService supply = new SupplyConnectivityService(gameState);
                sender.sendMessage("§6[국가 전략자원] §f" + nationId);
                for (StrategicResource resource : StrategicResource.values())
                    sender.sendMessage("§f" + resourceKorean(resource) + ": §e총 "
                            + supply.totalAmount(nationId, resource) + " / 가용 "
                            + supply.availableAmount(nationId, resource));
                return true;
            }
            if (sub.equals("set")) {
                requireArgs(args, 5, "/danta resource set <국가-id> <food|wood|iron|rare_mineral|mana_stone> <수량>");
                StrategicResourceStockpile stock = gameState.getOrCreateStrategicResourceStockpile(args[2]);
                StrategicResource resource = StrategicResource.valueOf(args[3].toUpperCase(Locale.ROOT));
                long amount = Long.parseLong(args[4]);
                stock.set(resource, amount);
                flushEconomyState("resource-set:" + args[2] + ":" + resource.name());
                sender.sendMessage("§a전략자원을 설정했습니다: §f" + resourceKorean(resource) + " §e" + amount);
                return true;
            }
            sender.sendMessage("§e사용법: /danta resource <show|set|available|local-show> <국가/거점-id> [자원] [수량]");
        } catch (NumberFormatException ex) {
            sender.sendMessage("§c수량은 정수로 입력해 주세요.");
        } catch (IllegalArgumentException ex) {
            sender.sendMessage("§c전략자원 작업을 처리할 수 없습니다: " + ex.getMessage());
        } catch (RuntimeException ex) {
            getLogger().warning("[StrategicResource] command failed: " + rootMessage(ex));
            sender.sendMessage("§c전략자원 작업 중 오류가 발생했습니다. 서버 콘솔을 확인해 주세요.");
        }
        return true;
    }

    private static String resourceKorean(StrategicResource resource) {
        return switch (resource) {
            case FOOD -> "식량";
            case WOOD -> "목재";
            case IRON -> "철";
            case RARE_MINERAL -> "희귀광물";
            case MANA_STONE -> "마력석";
        };
    }

    private void flushEconomyState(String reason) {
        if (snapshotService == null || databaseService == null) return;
        if (!databaseService.health().status().name().equals("READY")) return;
        snapshotService.flushImportantAsync(reason).whenComplete((ignored, error) -> {
            if (error != null) getLogger().warning("DEV-050 economy snapshot flush failed: " + rootMessage(error));
        });
    }

    private boolean handleEconomy(CommandSender sender, String[] args) {
        if (!sender.hasPermission("danta.admin.nation")) {
            sender.sendMessage("§c경제 관리 권한이 없습니다.");
            return true;
        }
        String sub = args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : "show";
        try {
            switch (sub) {
                case "wallet-set" -> {
                    requireArgs(args, 4, "/danta economy wallet-set <플레이어-id> <금액>");
                    long amount = Long.parseLong(args[3]);
                    PersonalWallet wallet = gameState.getOrCreatePersonalWallet(args[2]);
                    long current = wallet.balance();
                    if (amount < 0) throw new IllegalArgumentException("금액은 0 이상이어야 합니다.");
                    if (amount > current) wallet.deposit(amount - current);
                    else if (amount < current && !wallet.tryWithdraw(current - amount))
                        throw new IllegalStateException("개인지갑 금액 변경에 실패했습니다.");
                    flushEconomyState("wallet-set:" + args[2]);
                    sender.sendMessage("§a개인지갑을 설정했습니다: §e" + args[2] + " §7잔액=" + wallet.balance() + "G");
                }
                case "show" -> {
                    requireArgs(args, 4, "/danta economy show <플레이어-id> <국가-id>");
                    PersonalWallet wallet = gameState.getOrCreatePersonalWallet(args[2]);
                    NationState nation = gameState.nation(args[3])
                            .orElseThrow(() -> new IllegalArgumentException("국가를 찾을 수 없습니다: " + args[3]));
                    sender.sendMessage("§6[경제 상태]");
                    sender.sendMessage("§f개인지갑: §e" + wallet.balance() + "G");
                    sender.sendMessage("§f국고(" + nation.displayName() + "): §e" + nation.treasury() + "G");
                }
                case "admin" -> {
                    requireArgs(args, 3, "/danta economy admin <국가-id>");
                    NationState nation = gameState.nation(args[2])
                            .orElseThrow(() -> new IllegalArgumentException("국가를 찾을 수 없습니다: " + args[2]));
                    var status = new AdministrativeCapacityService(gameState).status(nation.nationId());
                    sender.sendMessage("§6[행정수용력] §f" + nation.displayName());
                    sender.sendMessage("§f보유 거점: §e" + status.ownedPoints());
                    sender.sendMessage("§f행정수요: §e" + String.format(Locale.ROOT, "%.2f", status.demand())
                            + " §7/ 수용력 " + String.format(Locale.ROOT, "%.2f", status.capacity()));
                    sender.sendMessage("§f과확장: " + (status.overextended() ? "§c예" : "§a아니요"));
                    sender.sendMessage("§f세입 효율: §e" + Math.round(status.revenueMultiplier() * 100.0) + "%");
                }
                case "contribute" -> {
                    requireArgs(args, 5, "/danta economy contribute <플레이어-id> <국가-id> <금액>");
                    long amount = Long.parseLong(args[4]);
                    var result = new EconomyTransferService(gameState).contributeToTreasury(args[2], args[3], amount);
                    if (!result.transferred()) {
                        sender.sendMessage("§c개인지갑 잔액이 부족해 출자할 수 없습니다.");
                    } else {
                        flushEconomyState("contribute:" + args[2] + ":" + args[3]);
                        sender.sendMessage("§a국고에 출자했습니다: §e" + amount + "G §7개인지갑="
                                + result.personalBalance() + "G, 국고=" + result.treasuryBalance() + "G");
                    }
                }
                case "withdraw" -> sender.sendMessage("§c국고에서 개인지갑으로 직접 인출할 수 없습니다.");
                default -> sender.sendMessage("§e사용법: /danta economy <wallet-set|show|admin|contribute|withdraw>");
            }
        } catch (NumberFormatException ex) {
            sender.sendMessage("§c금액은 정수로 입력해 주세요.");
        } catch (IllegalArgumentException ex) {
            sender.sendMessage("§c경제 작업을 처리할 수 없습니다: " + ex.getMessage());
        } catch (RuntimeException ex) {
            getLogger().warning("[Economy] command failed: " + rootMessage(ex));
            sender.sendMessage("§c경제 작업 중 오류가 발생했습니다. 서버 콘솔을 확인해 주세요.");
        }
        return true;
    }

    private boolean handleCombat(CommandSender sender, String[] args) {
        if (!sender.hasPermission("danta.admin.army")) {
            sender.sendMessage("§c전투 테스트 권한이 없습니다.");
            return true;
        }
        String sub = args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : "";
        if (!sub.equals("test")) {
            sender.sendMessage("§e사용법: /danta combat test");
            return true;
        }

        CombatResolver resolver = new CombatResolver();
        var powerResult = resolver.resolve(
                CombatSideInput.neutral("red", TroopType.INFANTRY, 1000),
                CombatSideInput.neutral("blue", TroopType.SPEARMEN, 1000));
        var resolution = new CombatLossPolicy().apply(powerResult);
        CombatReport report = CombatReport.from(resolution);
        for (String line : new CombatReportFormatter().formatKorean(report)) {
            sender.sendMessage(line);
        }
        return true;
    }

    private boolean handleArmy(CommandSender sender, String[] args) {
        if (!sender.hasPermission("danta.admin.army")) {
            sender.sendMessage("§c군단 관리 권한이 없습니다.");
            return true;
        }
        String sub = args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : "list";
        try {
            switch (sub) {
                case "create" -> {
                    requireArgs(args, 6, "/danta army create <군단-id> <국가-id> <거점-id> <기본-병력>");
                    ArmyState army = new ArmyState(args[2], args[3], args[4], ArmyStatus.STATIONED,
                            Long.parseLong(args[5]));
                    gameState.addArmy(army);
                    flushArmyState("army-create:" + army.armyId());
                    sender.sendMessage("§a군단을 생성했습니다: §e" + army.armyId() + " §7소유국="
                            + army.ownerNationId() + ", 위치=" + army.locationPointId());
                }
                case "list" -> {
                    sender.sendMessage("§6[단타 군단 목록] §7총 " + gameState.armies().size() + "개");
                    for (ArmyState army : gameState.armies()) sendArmy(sender, army);
                }
                case "gui" -> {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("§c군단 GUI는 게임 안에서만 열 수 있습니다.");
                    } else {
                        armyGuiController.open(player);
                    }
                }
                case "show" -> {
                    requireArgs(args, 3, "/danta army show <군단-id>");
                    sendArmy(sender, requireArmy(args[2]));
                }
                case "troops" -> {
                    requireArgs(args, 4, "/danta army troops <군단-id> <기본-병력>");
                    ArmyState army = requireArmy(args[2]);
                    army.setBaseTroops(Long.parseLong(args[3]));
                    flushArmyState("army-troops:" + army.armyId());
                    sender.sendMessage("§a군단의 기본 병력을 변경했습니다: §e" + army.baseTroops());
                }
                case "status" -> {
                    requireArgs(args, 4, "/danta army status <군단-id> <STATIONED|MOVING|IN_BATTLE>");
                    ArmyState army = requireArmy(args[2]);
                    army.setStatus(ArmyStatus.valueOf(args[3].toUpperCase(Locale.ROOT)));
                    flushArmyState("army-status:" + army.armyId());
                    sender.sendMessage("§a군단 상태를 변경했습니다: §e" + UiText.armyStatus(army.status()));
                }
                case "location" -> {
                    requireArgs(args, 4, "/danta army location <군단-id> <거점-id>");
                    ArmyState army = requireArmy(args[2]);
                    requireStrategicPoint(args[3]);
                    army.setLocationPointId(args[3]);
                    flushArmyState("army-location:" + army.armyId());
                    sender.sendMessage("§a군단 위치를 변경했습니다: §e" + army.locationPointId());
                }
                case "supply" -> {
                    requireArgs(args, 4, "/danta army supply <군단-id> <LIGHT|STANDARD|HEAVY>");
                    ArmyState army = requireArmy(args[2]);
                    ExpeditionSupplyLevel level = ExpeditionSupplyLevel.valueOf(args[3].toUpperCase(Locale.ROOT));
                    ExpeditionSupplyService.LoadResult result = expeditionSupplyService.load(army.armyId(), level);
                    flushArmyState("army-expedition-supply:" + army.armyId());
                    sender.sendMessage("§a출정 보급을 적재했습니다: §e" + level.displayName()
                            + " §7식량=" + result.loadedFood() + ", 국가 잔여 식량=" + result.nationFoodRemaining());
                }
                case "move" -> {
                    requireArgs(args, 4, "/danta army move <군단-id> <목적지-거점-id>");
                    ArmyOrder order = armyOrderService.issueMoveOrder(args[2], args[3]);
                    ArmyMovementTime movementTime = armyMovementTimeService.calculateForArmy(order.armyId());
                    RuntimeScheduledTask task = runtimeScheduler.scheduleAfter(
                            movementTime.effectiveDuration(), "army.move.arrive",
                            Map.of("armyId", order.armyId(), "orderId", order.orderId()));
                    ArmyState movingArmy = requireArmy(order.armyId());
                    movingArmy.setStatus(ArmyStatus.MOVING);
                    updateActiveMovement(order, task.dueRuntimeMillis());
                    flushArmyState("army-move-start:" + movingArmy.armyId());
                    sender.sendMessage("§a이동을 시작했습니다: §e" + order.orderId());
                    sender.sendMessage("§f예상 이동시간: §e" + formatRuntime(movementTime.effectiveDuration().toMillis()));
                    sender.sendMessage("§f도착 예정 서버시간: §e" + formatRuntime(task.dueRuntimeMillis()));
                    sendArmyOrder(sender, order);
                }
                case "order" -> {
                    requireArgs(args, 3, "/danta army order <군단-id>");
                    ArmyState army = requireArmy(args[2]);
                    var order = gameState.armyOrder(army.armyId());
                    if (order.isEmpty()) sender.sendMessage("§7현재 등록된 군단 명령이 없습니다: §e" + army.armyId());
                    else sendArmyOrder(sender, order.get());
                }
                case "queue" -> {
                    requireArgs(args, 4, "/danta army queue <군단-id> <목적지1> [목적지2 ...]");
                    ArmyOperationQueue queue = armyOperationQueueService.create(
                            args[2], Arrays.asList(Arrays.copyOfRange(args, 3, args.length)));
                    armyOperationQueues.put(queue.armyId(), queue);
                    syncOperationQueuesToSnapshot();
                    startQueuedLeg(queue.armyId());
                    flushArmyState("army-operation-queue:" + queue.armyId());
                    sender.sendMessage("§a연속 작전을 시작했습니다: §e" + queue.armyId());
                    sender.sendMessage("§f예약 경로: §e" + String.join(" §f-> §e", queue.destinations()));
                }
                case "queue-show" -> {
                    requireArgs(args, 3, "/danta army queue-show <군단-id>");
                    ArmyOperationQueue queue = armyOperationQueues.get(args[2]);
                    if (queue == null) sender.sendMessage("§7현재 등록된 연속 작전이 없습니다: §e" + args[2]);
                    else sender.sendMessage("§6[연속 작전] §e" + queue.armyId() + " §f남은 경로: §e"
                            + String.join(" §f-> §e", queue.destinations()));
                }
                case "eta" -> {
                    requireArgs(args, 3, "/danta army eta <군단-id>");
                    ArmyMovementTime movementTime = armyMovementTimeService.calculateForArmy(args[2]);
                    sender.sendMessage("§6[군단 이동시간] §e" + args[2]);
                    sender.sendMessage("§f간선 기본시간: §e" + formatRuntime(movementTime.baseDuration().toMillis()));
                    sender.sendMessage("§f경로 수정치: §ex" + String.format(Locale.ROOT, "%.3f", movementTime.multiplier()));
                    sender.sendMessage("§f예상 이동시간: §e" + formatRuntime(movementTime.effectiveDuration().toMillis()));
                }
                default -> sender.sendMessage("§e/danta army <create|list|gui|show|troops|status|location|supply|move|order|eta|queue|queue-show>");
            }
        } catch (RuntimeException ex) {
            sendCommandError(sender, "군단", ex);
        }
        return true;
    }

    private ArmyState requireArmy(String armyId) {
        return gameState.army(armyId)
                .orElseThrow(() -> new IllegalArgumentException("army not found: " + armyId));
    }

    private void sendArmy(CommandSender sender, ArmyState army) {
        sender.sendMessage("§e" + army.armyId() + " §7소유국=" + army.ownerNationId()
                + ", 위치=" + army.locationPointId() + ", 상태=" + UiText.armyStatus(army.status())
                + ", 기본 병력=" + army.baseTroops()
                + ", 출정보급=" + (army.expeditionSupplyLevel() == null ? "미적재" : army.expeditionSupplyLevel().displayName())
                + ", 휴대식량=" + army.carriedFood());
    }

    private void sendArmyOrder(CommandSender sender, ArmyOrder order) {
        sender.sendMessage("§6[군단 명령] §e" + order.orderId());
        sender.sendMessage("§f군단: §e" + order.armyId() + " §7종류=" + UiText.armyOrderType(order.type())
                + ", 상태=" + UiText.armyOrderStatus(order.status()));
        sender.sendMessage("§f경로: §e" + order.route().originPointId() + " §f-> §e"
                + order.route().destinationPointId() + " §7간선=" + order.route().edgeId());
    }

    private void flushArmyState(String reason) {
        if (snapshotService == null || databaseService == null) return;
        if (!databaseService.health().status().name().equals("READY")) return;
        snapshotService.flushImportantAsync(reason).whenComplete((ignored, error) -> {
            if (error != null) getLogger().warning("DEV-030 army snapshot flush failed: " + rootMessage(error));
        });
    }

    private boolean handleNation(CommandSender sender, String[] args) {
        if (!sender.hasPermission("danta.admin.nation")) {
            sender.sendMessage("§c국가 관리 권한이 없습니다.");
            return true;
        }
        String sub = args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : "list";
        try {
            switch (sub) {
                case "create" -> {
                    requireArgs(args, 4, "/danta nation create <국가-id> <표시-이름>");
                    String id = args[2];
                    String name = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                    NationState nation = new NationState(id, name);
                    gameState.addNation(nation);
                    flushNationState("nation-create:" + id);
                    sender.sendMessage("§a국가를 생성했습니다: §e" + id + " §f(" + name + ")");
                }
                case "list" -> {
                    sender.sendMessage("§6[단타 국가 목록] §7총 " + gameState.nations().size() + "개");
                    for (NationState nation : gameState.nations()) {
                        sender.sendMessage("§e" + nation.nationId() + " §f" + nation.displayName()
                                + " §7수도=" + nation.capitalPointId().orElse("없음")
                                + ", 국고=" + nation.treasury() + "G, 상태=" + UiText.nationStatus(nation.status()));
                    }
                }
                case "show" -> {
                    requireArgs(args, 3, "/danta nation show <국가-id>");
                    sendNation(sender, requireNation(args[2]));
                }
                case "capital" -> {
                    requireArgs(args, 4, "/danta nation capital <국가-id> <거점-id|none>");
                    NationState nation = requireNation(args[2]);
                    String capitalPointId = args[3].equalsIgnoreCase("none") ? null : args[3];
                    if (capitalPointId != null) requireStrategicPoint(capitalPointId);
                    nation.setCapitalPointId(capitalPointId);
                    flushNationState("nation-capital:" + nation.nationId());
                    sender.sendMessage("§a수도를 변경했습니다: §e" + nation.capitalPointId().orElse("없음"));
                }
                case "treasury" -> {
                    requireArgs(args, 4, "/danta nation treasury <국가-id> <금화>");
                    NationState nation = requireNation(args[2]);
                    nation.setTreasury(Long.parseLong(args[3]));
                    flushNationState("nation-treasury:" + nation.nationId());
                    sender.sendMessage("§a국고를 변경했습니다: §e" + nation.treasury() + "G");
                }
                case "status" -> {
                    requireArgs(args, 4, "/danta nation status <국가-id> <ACTIVE|VASSAL>");
                    NationState nation = requireNation(args[2]);
                    nation.setStatus(NationStatus.valueOf(args[3].toUpperCase(Locale.ROOT)));
                    flushNationState("nation-status:" + nation.nationId());
                    sender.sendMessage("§a국가 상태를 변경했습니다: §e" + UiText.nationStatus(nation.status()));
                }
                default -> sender.sendMessage("§e/danta nation <create|list|show|capital|treasury|status>");
            }
        } catch (RuntimeException ex) {
            sendCommandError(sender, "국가", ex);
        }
        return true;
    }

    private NationState requireNation(String nationId) {
        return gameState.nation(nationId)
                .orElseThrow(() -> new IllegalArgumentException("nation not found: " + nationId));
    }

    private void sendNation(CommandSender sender, NationState nation) {
        sender.sendMessage("§6[단타 국가] §e" + nation.nationId());
        sender.sendMessage("§f이름: §e" + nation.displayName());
        sender.sendMessage("§f수도: §e" + nation.capitalPointId().orElse("없음"));
        sender.sendMessage("§f국고: §e" + nation.treasury() + "G");
        sender.sendMessage("§f상태: §e" + UiText.nationStatus(nation.status()));
    }

    private void flushNationState(String reason) {
        if (snapshotService == null || databaseService == null) return;
        if (!databaseService.health().status().name().equals("READY")) return;
        snapshotService.flushImportantAsync(reason).whenComplete((ignored, error) -> {
            if (error != null) getLogger().warning("DEV-020 nation snapshot flush failed: " + rootMessage(error));
        });
    }

    private void startSnapshotTask() {
        if (snapshotTask != null) snapshotTask.cancel();
        // Persistence housekeeping: every 5 real minutes. This does not advance game/runtime time.
        snapshotTask = getServer().getScheduler().runTaskTimer(this, () -> {
            if (snapshotService != null) snapshotService.saveAsync("periodic");
        }, 20L * 60L * 5L, 20L * 60L * 5L);
    }

    private boolean handleSnapshot(CommandSender sender, String[] args) {
        if (!sender.hasPermission("danta.admin.db")) {
            sender.sendMessage("§c스냅샷 관리 권한이 없습니다.");
            return true;
        }
        if (snapshotService == null || databaseService == null || databaseService.health().status().name().equals("DISABLED")) {
            sender.sendMessage("§c스냅샷 기능을 사용하려면 PostgreSQL이 활성화되어 있어야 합니다.");
            return true;
        }
        String sub = args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : "save";
        switch (sub) {
            case "save" -> {
                sender.sendMessage("§e스냅샷을 비동기로 저장하고 있습니다...");
                snapshotService.saveAsync("manual").whenComplete((ignored, error) -> runSync(() -> {
                    if (error == null) sender.sendMessage("§a스냅샷을 저장했습니다.");
                    else sendAsyncFailure(sender, "스냅샷 저장에 실패했습니다.", "Manual snapshot save failed", error);
                }));
            }
            case "important" -> {
                String reason = args.length >= 3 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "manual-test";
                sender.sendMessage("§e중요 스냅샷을 비동기로 저장하고 있습니다...");
                snapshotService.flushImportantAsync(reason).whenComplete((ignored, error) -> runSync(() -> {
                    if (error == null) sender.sendMessage("§a중요 스냅샷 저장을 완료했습니다.");
                    else sendAsyncFailure(sender, "중요 스냅샷 저장에 실패했습니다.", "Important snapshot flush failed", error);
                }));
            }
            case "load" -> {
                sender.sendMessage("§e스냅샷을 비동기로 불러오고 있습니다...");
                snapshotService.loadAsync().whenComplete((snapshot, error) -> runSync(() -> {
                    if (error != null) sendAsyncFailure(sender, "스냅샷 불러오기에 실패했습니다.", "Manual snapshot load failed", error);
                    else if (snapshot.isEmpty()) sender.sendMessage("§7저장된 스냅샷이 없습니다.");
                    else {
                        snapshotService.apply(snapshot.get());
                        persistRuntime();
                        sender.sendMessage("§a스냅샷을 복원했습니다. 서버 시간=§e" + formatRuntime(runtimeClock.elapsedMillis()));
                    }
                }));
            }
            default -> sender.sendMessage("§e/danta snapshot <save|important [사유]|load>");
        }
        return true;
    }
    private boolean handleDatabase(CommandSender sender, String[] args) {
        if (!sender.hasPermission("danta.admin.db")) {
            sender.sendMessage("§c데이터베이스 관리 권한이 없습니다.");
            return true;
        }
        if (databaseService == null) {
            sender.sendMessage("§c데이터베이스 기능이 초기화되지 않았습니다. 서버 콘솔과 설정을 확인해 주세요.");
            return true;
        }

        String sub = args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : "status";
        try {
            switch (sub) {
                case "status" -> sendDatabaseStatus(sender);
                case "reconnect" -> {
                    sender.sendMessage("§ePostgreSQL 연결을 비동기로 확인하고 있습니다...");
                    databaseService.initializeAsync().whenComplete((ready, error) -> runSync(() -> {
                        if (error != null || !Boolean.TRUE.equals(ready)) {
                            if (error != null) getLogger().warning("PostgreSQL reconnect failed: " + rootMessage(error));
                            sender.sendMessage("§cPostgreSQL 연결에 실패했습니다. /danta db status로 상태를 확인해 주세요.");
                        } else {
                            sender.sendMessage("§aPostgreSQL 연결이 정상입니다.");
                        }
                    }));
                }
                case "ping" -> {
                    sender.sendMessage("§ePostgreSQL 응답을 비동기로 확인하고 있습니다...");
                    databaseService.pingAsync().whenComplete((ok, error) -> runSync(() -> {
                        if (error != null) sendAsyncFailure(sender, "DB 응답 확인에 실패했습니다.", "Database ping failed", error);
                        else sender.sendMessage(Boolean.TRUE.equals(ok) ? "§aDB 응답이 정상입니다." : "§cDB 응답이 올바르지 않습니다.");
                    }));
                }
                case "put" -> {
                    requireArgs(args, 4, "/danta db put <키> <값>");
                    String key = args[2];
                    String value = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                    devRepository.save("dev016", key, value).whenComplete((ignored, error) -> runSync(() -> {
                        if (error != null) sendAsyncFailure(sender, "DB 저장에 실패했습니다.", "Database write failed", error);
                        else sender.sendMessage("§a비동기 저장을 완료했습니다: §e" + key);
                    }));
                }
                case "get" -> {
                    requireArgs(args, 3, "/danta db get <키>");
                    String key = args[2];
                    devRepository.find("dev016", key).whenComplete((value, error) -> runSync(() -> {
                        if (error != null) sendAsyncFailure(sender, "DB 조회에 실패했습니다.", "Database read failed", error);
                        else sender.sendMessage(value.map(v -> "§a" + key + " = §e" + v)
                                .orElse("§7해당 키에 저장된 값이 없습니다: " + key));
                    }));
                }
                case "delete" -> {
                    requireArgs(args, 3, "/danta db delete <키>");
                    String key = args[2];
                    devRepository.delete("dev016", key).whenComplete((deleted, error) -> runSync(() -> {
                        if (error != null) sendAsyncFailure(sender, "DB 삭제에 실패했습니다.", "Database delete failed", error);
                        else sender.sendMessage(Boolean.TRUE.equals(deleted)
                                ? "§a삭제했습니다: §e" + key
                                : "§7삭제할 값이 없습니다: " + key);
                    }));
                }
                default -> sender.sendMessage("§e/danta db <status|reconnect|ping|put|get|delete>");
            }
        } catch (RuntimeException ex) {
            sendCommandError(sender, "데이터베이스", ex);
        }
        return true;
    }

    private void sendDatabaseStatus(CommandSender sender) {
        DatabaseHealth health = databaseService.health();
        sender.sendMessage("§6[단타 PostgreSQL]");
        sender.sendMessage("§f상태: §e" + UiText.databaseStatus(health.status()));
        sender.sendMessage("§f연결 대상: §e" + health.target());
        if (health.lastError() != null) sender.sendMessage("§f최근 오류: §c발생함(자세한 내용은 서버 콘솔 확인)");
        sender.sendMessage("§7설정 파일: plugins/DantaServer/database.properties");
    }

    private void requireSeasonAction(SeasonAction action) {
        var decision=seasonActionGate.check(action);
        if(!decision.allowed()) throw new IllegalStateException(decision.reason());
    }



    private boolean handleChronicle(CommandSender sender) {
        sender.sendMessage("§6[단타 연대기]");
        var entries = chronicleService.recent(20);
        if (entries.isEmpty()) {
            sender.sendMessage("§7아직 기록된 주요 사건이 없습니다.");
            return true;
        }
        for (var entry : entries) {
            sender.sendMessage("§7[" + formatRuntime(entry.runtimeMillis()) + "] §f" + entry.summary());
        }
        return true;
    }

    private boolean handleRanking(CommandSender sender) {
        var ranking = nationRankingService.ranking();
        sender.sendMessage("§6[단타 천하 순위]");
        if (ranking.isEmpty()) {
            sender.sendMessage("§7등록된 국가가 없습니다.");
            return true;
        }
        int rank = 1;
        for (var row : ranking) {
            sender.sendMessage("§e" + rank + "위 §f" + row.displayName()
                    + " §7- 총점 §6" + row.totalScore()
                    + " §7(누적 명성 §f" + row.fameScore()
                    + "§7 / 현재 패권 §f" + row.hegemonyScore() + "§7)");
            rank++;
        }
        if (seasonPhaseService.current() == kr.danta.core.season.SeasonPhase.FINISHED) {
            sender.sendMessage("§a시즌 최종 결과입니다.");
        } else {
            sender.sendMessage("§7현재 서버 상태를 기준으로 한 실시간 순위입니다.");
        }
        return true;
    }

    private boolean handleSeason(CommandSender sender, String[] args) {
        if (args.length >= 2 && args[1].equalsIgnoreCase("end")) {
            if (!sender.hasPermission("danta.admin.season")) {
                sender.sendMessage("§c시즌을 종료할 권한이 없습니다.");
                return true;
            }
            if (seasonPhaseService.current() == kr.danta.core.season.SeasonPhase.FINISHED) {
                sender.sendMessage("§c이미 시즌이 종료된 상태입니다.");
                return true;
            }
            long finishRuntime = java.time.Duration.ofHours(50).toMillis();
            runtimeClock.setElapsedMillis(finishRuntime);
            runtimeClock.pause();
            chronicleService.record(runtimeClock.elapsedMillis(), ChronicleEventType.SEASON_ENDED, "관리자에 의해 시즌이 종료됨");
            persistRuntime();
            if (snapshotService != null) snapshotService.flushImportantAsync("admin-season-end");
            sender.sendMessage("§6[단타] §f관리자에 의해 시즌이 조기 종료되었습니다.");
            getLogger().info("[Season] Administrator ended the season early: " + sender.getName());
            return true;
        }
        var phase = seasonPhaseService.current();
        sender.sendMessage("§6[단타 시즌 단계]");
        sender.sendMessage("§f현재 단계: §e" + phase.displayName() + " §7(" + phase.name() + ")");
        sender.sendMessage("§f서버 가동 시간: §e" + formatRuntime(seasonPhaseService.runtimeMillis()));
        if (phase != kr.danta.core.season.SeasonPhase.FINISHED) {
            sender.sendMessage("§f다음 단계까지: §e" + formatRuntime(seasonPhaseService.remainingMillis()));
        } else {
            sender.sendMessage("§7시즌이 종료되었습니다.");
        }
        return true;
    }

    private boolean handleRuntime(CommandSender sender, String[] args) {
        if (!sender.hasPermission("danta.admin.runtime")) {
            sender.sendMessage("§c서버 시간 관리 권한이 없습니다."); return true;
        }
        if (args.length == 1 || args[1].equalsIgnoreCase("status")) {
            sender.sendMessage("§6[단타 서버 시간] §e" + formatRuntime(runtimeClock.elapsedMillis())
                    + " §7일시정지=" + yesNo(runtimeClock.isPaused()) + ", 배속=x" + runtimeClock.speedMultiplier());
            return true;
        }
        try {
            switch (args[1].toLowerCase(Locale.ROOT)) {
                case "pause" -> runtimeClock.pause();
                case "resume" -> runtimeClock.resume();
                case "speed" -> {
                    requireArgs(args, 3, "/danta runtime speed <0.1~100>");
                    runtimeClock.setSpeedMultiplier(Double.parseDouble(args[2]));
                }
                case "set" -> {
                    requireArgs(args, 3, "/danta runtime set <초>");
                    long seconds = Long.parseLong(args[2]);
                    if (seconds < 0 || seconds > Long.MAX_VALUE / 1000L) throw new IllegalArgumentException("invalid seconds");
                    runtimeClock.setElapsedMillis(seconds * 1000L);
                }
                case "save" -> persistRuntime();
                case "schedule" -> {
                    requireArgs(args, 3, "/danta runtime schedule <서버시간-초> [메시지]");
                    long seconds = Long.parseLong(args[2]);
                    if (seconds < 0) throw new IllegalArgumentException("runtime-seconds must be >= 0");
                    String message = args.length >= 4
                            ? String.join(" ", Arrays.copyOfRange(args, 3, args.length))
                            : "DEV-015 test";
                    RuntimeScheduledTask task = runtimeScheduler.scheduleAfter(
                            Duration.ofSeconds(seconds), "dev.echo", Map.of("message", message));
                    sender.sendMessage("§a예약 작업을 등록했습니다: §e" + task.id()
                            + " §7실행 예정 시간=" + formatRuntime(task.dueRuntimeMillis()));
                    return true;
                }
                case "scheduler" -> {
                    sender.sendMessage("§6[단타 서버 시간 예약 작업] §e대기=" + runtimeScheduler.size() + "개");
                    runtimeScheduler.nextTask().ifPresentOrElse(
                            task -> sender.sendMessage("§f다음 작업: §e" + task.id()
                                    + " §7종류=" + task.taskType()
                                    + ", 실행 예정 시간=" + formatRuntime(task.dueRuntimeMillis())),
                            () -> sender.sendMessage("§7대기 중인 예약 작업이 없습니다."));
                    return true;
                }
                case "cancel" -> {
                    requireArgs(args, 3, "/danta runtime cancel <작업-uuid>");
                    UUID taskId = UUID.fromString(args[2]);
                    sender.sendMessage(runtimeScheduler.cancel(taskId)
                            ? "§a예약 작업을 취소했습니다: §e" + taskId
                            : "§c해당 예약 작업을 찾을 수 없습니다: " + taskId);
                    return true;
                }
                default -> {
                    sender.sendMessage("§e/danta runtime <status|pause|resume|speed|set|save|schedule|scheduler|cancel>");
                    return true;
                }
            }
            persistRuntime();
            sender.sendMessage("§a서버 시간을 변경했습니다: §e" + formatRuntime(runtimeClock.elapsedMillis())
                    + " §7일시정지=" + yesNo(runtimeClock.isPaused()) + ", 배속=x" + runtimeClock.speedMultiplier());
        } catch (RuntimeException ex) {
            sendCommandError(sender, "서버 시간", ex);
        }
        return true;
    }

    private void updateActiveMovement(ArmyOrder order, long dueRuntimeMillis) {
        ArmyOrderSnapshot movement = new ArmyOrderSnapshot(order.orderId(), order.armyId(), order.type(),
                order.route().originPointId(), order.route().destinationPointId(), order.route().edgeId(),
                order.status(), dueRuntimeMillis);
        pendingMovementSnapshots.put(order.armyId(), movement);
        if (snapshotService != null) {
            List<ArmyOrderSnapshot> updated = new java.util.ArrayList<>(snapshotService.restoredArmyOrders());
            updated.removeIf(existing -> existing.armyId().equals(order.armyId()));
            updated.add(movement);
            snapshotService.setActiveArmyMovements(updated);
        }
    }

    private void removeActiveMovement(String armyId) {
        pendingMovementSnapshots.remove(armyId);
        if (snapshotService != null) {
            List<ArmyOrderSnapshot> updated = new java.util.ArrayList<>(snapshotService.restoredArmyOrders());
            updated.removeIf(existing -> existing.armyId().equals(armyId));
            snapshotService.setActiveArmyMovements(updated);
        }
    }

    private void restoreArmyMovements() {
        if (snapshotService == null || runtimeScheduler == null) return;
        pendingMovementSnapshots.clear();
        armyOperationQueues.clear();
        for (ArmyOperationQueueSnapshot queue : snapshotService.armyOperationQueues()) {
            if (!queue.destinations().isEmpty()) armyOperationQueues.put(queue.armyId(), new ArmyOperationQueue(queue.armyId(), queue.destinations()));
        }
        for (ArmyOrderSnapshot movement : snapshotService.restoredArmyOrders()) {
            pendingMovementSnapshots.put(movement.armyId(), movement);
            RuntimeScheduledTask task = new RuntimeScheduledTask(
                    UUID.randomUUID(), movement.dueRuntimeMillis(), "army.move.arrive",
                    Map.of("armyId", movement.armyId(), "orderId", movement.orderId()));
            runtimeScheduler.restore(task);
        }
        // Execute already-due arrivals immediately against restored server runtime.
        pumpRuntimeScheduler();
    }

    private void completeArmyMovement(String armyId, String orderId) {
        if (armyId == null || orderId == null) throw new IllegalArgumentException("movement task payload is incomplete");
        ArmyState army = requireArmy(armyId);
        ArmyOrder order = gameState.armyOrder(armyId)
                .orElseThrow(() -> new IllegalStateException("movement order missing for army: " + armyId));
        if (!order.orderId().equals(orderId)) {
            throw new IllegalStateException("movement order id mismatch for army: " + armyId);
        }
        army.setLocationPointId(order.route().destinationPointId());
        army.setStatus(ArmyStatus.STATIONED);
        gameState.removeArmyOrder(armyId);
        removeActiveMovement(armyId);

        ArmyOperationQueue queue = armyOperationQueues.get(armyId);
        if (queue != null) {
            if (queue.hasFollowingLeg()) {
                ArmyOperationQueue remaining = queue.afterArrival();
                ArmyAdvanceDecision decision = armyAdvanceStopPolicy.evaluateAfterArrival(armyId);
                if (decision.shouldStop()) {
                    armyOperationQueues.remove(armyId);
                    syncOperationQueuesToSnapshot();
                    getLogger().info("[ArmyAdvance] operation stopped: army=" + armyId
                            + ", reason=" + decision.reason() + ", point=" + army.locationPointId());
                } else {
                    armyOperationQueues.put(armyId, remaining);
                    syncOperationQueuesToSnapshot();
                    startQueuedLeg(armyId);
                }
            } else {
                armyOperationQueues.remove(armyId);
                syncOperationQueuesToSnapshot();
            }
        }
        flushArmyState("army-move-arrive:" + armyId);
        getLogger().info("[ArmyMovement] arrived: army=" + armyId + ", point=" + army.locationPointId());
    }

    private void startQueuedLeg(String armyId) {
        ArmyOperationQueue queue = armyOperationQueues.get(armyId);
        if (queue == null) return;
        ArmyOrder order = armyOrderService.issueMoveOrder(armyId, queue.nextDestination());
        ArmyMovementTime movementTime = armyMovementTimeService.calculateForArmy(armyId);
        RuntimeScheduledTask task = runtimeScheduler.scheduleAfter(
                movementTime.effectiveDuration(), "army.move.arrive",
                Map.of("armyId", armyId, "orderId", order.orderId()));
        requireArmy(armyId).setStatus(ArmyStatus.MOVING);
        updateActiveMovement(order, task.dueRuntimeMillis());
        syncOperationQueuesToSnapshot();
    }

    private void syncOperationQueuesToSnapshot() {
        if (snapshotService == null) return;
        snapshotService.setArmyOperationQueues(armyOperationQueues.values().stream()
                .map(queue -> new ArmyOperationQueueSnapshot(queue.armyId(), queue.destinations()))
                .toList());
    }

    private void pumpRuntimeScheduler() {
        if (runtimeScheduler == null) return;
        pumpEconomyTicks();
        pumpIndependenceWars();
        for (RuntimeTaskExecution execution : runtimeScheduler.executeDueTasks()) {
            if (!execution.success()) {
                getLogger().severe("Runtime task failed: id=" + execution.task().id()
                        + ", type=" + execution.task().taskType()
                        + ", error=" + execution.error().getMessage());
            }
        }
    }

    private void pumpIndependenceWars() {
        if (independenceWarService == null || runtimeClock == null) return;
        for (var state : independenceWarService.states()) {
            if (!state.active()) continue;
            var outcome = independenceWarService.tick(state.vassalNationId(), runtimeClock.elapsedMillis());
            if (outcome != IndependenceWarService.Outcome.ONGOING) endIndependenceParticipation(state);
            if (outcome == IndependenceWarService.Outcome.INDEPENDENCE_SUCCESS) {
                getLogger().info("[IndependenceWar] independence succeeded: " + state.vassalNationId());
                flushDiplomacyState("independence-success:" + state.vassalNationId());
            } else if (outcome == IndependenceWarService.Outcome.FAILED_CAPITAL_LOST) {
                getLogger().info("[IndependenceWar] failed by capital loss: " + state.vassalNationId());
                flushDiplomacyState("independence-failed:" + state.vassalNationId());
            }
        }
    }

    private void endIndependenceParticipation(kr.danta.core.nation.IndependenceWarState state) {
        if (warService == null || state == null) return;
        warService.findBetween(state.vassalNationId(), state.overlordNationId()).ifPresent(war -> {
            warService.endWar(war.warId());
            getLogger().info("[IndependenceWar] participation closed: " + war.warId());
        });
    }

    private void pumpEconomyTicks() {
        if (economyTickService == null || runtimeClock == null) return;
        int due = economyTickService.claimDueTicks(runtimeClock.elapsedMillis());
        if (due <= 0) return;
        economyTicksProcessed += due;
        long supplyRequested = 0L, supplyConsumed = 0L;
        int supplyShortfalls = 0;
        for (int i = 0; i < due; i++) {
            strategicPointProductionService.produceOneTick();
            ArmySupplyService.TickResult supply = armySupplyService.consumeOneTick();
            supplyRequested = Math.addExact(supplyRequested, supply.requestedFood());
            supplyConsumed = Math.addExact(supplyConsumed, supply.consumedFood());
            supplyShortfalls += supply.shortfallArmies();
        }
        flushEconomyState("economy-tick:" + economyTicksProcessed);
        getLogger().info("[EconomyTick] " + due + "회 처리, 누적=" + economyTicksProcessed
                + ", runtime=" + formatRuntime(runtimeClock.elapsedMillis())
                + ", 군단식량=" + supplyConsumed + "/" + supplyRequested
                + (supplyShortfalls > 0 ? ", 보급부족군단=" + supplyShortfalls : ""));
    }

    private void runSync(Runnable action) {
        if (!isEnabled()) return;
        getServer().getScheduler().runTask(this, action);
    }

    private void sendCommandError(CommandSender sender, String context, RuntimeException error) {
        getLogger().warning("Player command failed: context=" + context + ", error=" + rootMessage(error));
        sender.sendMessage("§c" + context + " 요청을 처리하지 못했습니다: " + UiText.playerError(error));
    }

    private void sendAsyncFailure(CommandSender sender, String playerMessage, String logContext, Throwable error) {
        getLogger().warning(logContext + ": " + rootMessage(error));
        sender.sendMessage("§c" + playerMessage + " 자세한 내용은 서버 콘솔을 확인해 주세요.");
    }

    private static String yesNo(boolean value) {
        return value ? "예" : "아니요";
    }

    private static String rootMessage(Throwable error) {
        Throwable current = error;
        while ((current instanceof CompletionException || current.getCause() != null) && current.getCause() != null) {
            current = current.getCause();
        }
        String msg = current.getMessage();
        return current.getClass().getSimpleName() + (msg == null ? "" : ": " + msg);
    }

    private static void requireArgs(String[] args, int count, String usage) {
        if (args.length < count) throw new IllegalArgumentException("Usage: " + usage);
    }

    private static String formatRuntime(long millis) {
        long s = Math.max(0L, millis / 1000L);
        return String.format("%02d:%02d:%02d", s / 3600L, (s % 3600L) / 60L, s % 60L);
    }

    private boolean handleDiplomacy(CommandSender sender, String[] args) {
        if (!sender.hasPermission("danta.admin.point")) { sender.sendMessage("§c외교 관리 권한이 없습니다."); return true; }
        try {
            String sub = args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : "list";
            switch (sub) {
                case "vassalize" -> { requireArgs(args,4,"/danta diplomacy vassalize <패배국> <종주국>"); var r=vassalService.onCapitalFallen(args[2],args[3],runtimeClock.elapsedMillis()); flushDiplomacyState("vassalize:"+r.vassalNationId()+":"+r.overlordNationId()); sender.sendMessage("§c수도 함락으로 속국이 되었습니다: §e"+r.vassalNationId()+" §7종주국="+r.overlordNationId()); sender.sendMessage("§7국가·잔여 영토·연구·군단은 유지됩니다. 조공과 군사외교 제한은 후속 규칙에서 적용됩니다."); }
                case "independence-declare" -> {
                    requireArgs(args,3,"/danta diplomacy independence-declare <속국-id>");
                    var s=independenceWarService.declare(args[2],runtimeClock.elapsedMillis());
                    var independenceWar=warService.declareIndependenceWar(s.vassalNationId(),s.overlordNationId());
                    flushDiplomacyState("independence-declare:"+s.vassalNationId());
                    sender.sendMessage("§c독립전쟁을 선언했습니다: §e"+s.vassalNationId()+" §7vs "+s.overlordNationId()+" §7전쟁 ID="+independenceWar.warId());
                    sender.sendMessage("§7목표: 자기 수도를 서버 러닝타임 기준으로 계속 방어하십시오.");
                    sender.sendMessage("§7남은 방어시간: §e"+formatRuntime(Math.max(0L,s.holdUntilRuntimeMillis()-runtimeClock.elapsedMillis()))+" §8(개발용 임시값)");
                }
                case "independence-status" -> {
                    requireArgs(args,3,"/danta diplomacy independence-status <국가-id>");
                    var before=independenceWarService.state(args[2]).orElse(null);
                    var outcome=independenceWarService.tick(args[2],runtimeClock.elapsedMillis());
                    if(outcome!=IndependenceWarService.Outcome.ONGOING && before!=null) endIndependenceParticipation(before);
                    if(outcome!=IndependenceWarService.Outcome.ONGOING) flushDiplomacyState("independence-outcome:"+args[2]);
                    var s=independenceWarService.state(args[2]).orElse(null);
                    if(vassalService.relation(args[2]).isEmpty()){sender.sendMessage("§6[독립전쟁] §e"+args[2]+" §a독립국");}
                    else if(s==null){var r=vassalService.relation(args[2]).orElseThrow();long ready=r.vassalizedAtRuntimeMillis()+independenceWarService.minimumSubordinationMillis();sender.sendMessage("§6[독립전쟁] §e"+args[2]+" §7미선언 / 선언 가능까지 서버시간≈"+formatRuntime(Math.max(0L,ready-runtimeClock.elapsedMillis())));}
                    else if(s.active()){sender.sendMessage("§6[독립전쟁] §e진행 중 §7/ 수도 방어 남은 서버시간≈"+formatRuntime(Math.max(0L,s.holdUntilRuntimeMillis()-runtimeClock.elapsedMillis())));}
                    else{sender.sendMessage("§6[독립전쟁] §c실패 §7/ 재선언 가능까지 서버시간≈"+formatRuntime(Math.max(0L,s.redeclareAfterRuntimeMillis()-runtimeClock.elapsedMillis())));}
                }
                case "independence-support-join" -> {
                    requireArgs(args,4,"/danta diplomacy independence-support-join <지원국> <독립국>");
                    String supporter=args[2], vassal=args[3];
                    var state=independenceWarService.state(vassal).filter(kr.danta.core.nation.IndependenceWarState::active)
                            .orElseThrow(()->new IllegalStateException("independence war is not active"));
                    if(supporter.equals(vassal)) throw new IllegalArgumentException("nation cannot support itself");
                    if(supporter.equals(state.overlordNationId())) throw new IllegalStateException("overlord cannot join independence side");
                    var war=warService.findBetween(vassal,state.overlordNationId())
                            .orElseThrow(()->new IllegalStateException("independence war participation record not found"));
                    warService.supportJoin(war.warId(),supporter,WarSide.ATTACKER);
                    flushDiplomacyState("independence-support-join:"+supporter+":"+vassal);
                    sender.sendMessage("§a독립전쟁에 공식 참전했습니다: §e"+supporter+" §7→ 독립측 "+vassal);
                    sender.sendMessage("§7전쟁 ID="+war.warId()+" / 독립측="+String.join(", ",war.attackers())+" / 종주국측="+String.join(", ",war.defenders()));
                }
                case "independence-support-gold" -> {
                    requireArgs(args,5,"/danta diplomacy independence-support-gold <지원국> <속국> <금화>");
                    long amount=Long.parseLong(args[4]);
                    var r=independenceSupportService.transferGold(args[2],args[3],amount);
                    flushEconomyState("independence-support-gold:"+r.supporterNationId()+":"+r.recipientNationId());
                    sender.sendMessage("§a독립 준비 자금을 지원했습니다: §e"+r.supporterNationId()+" → "+r.recipientNationId()+" §7"+r.amount()+"G");
                    sender.sendMessage("§7지원국 국고="+r.supporterTreasury()+"G / 수령국 국고="+r.recipientTreasury()+"G");
                    sender.sendMessage("§8이 지원은 독립전쟁을 자동으로 시작하거나 선언 조건을 우회하지 않습니다.");
                }
                case "independence-support-resource" -> {
                    requireArgs(args,6,"/danta diplomacy independence-support-resource <지원국> <속국> <자원> <수량>");
                    StrategicResource resource=StrategicResource.valueOf(args[4].toUpperCase(Locale.ROOT));
                    long amount=Long.parseLong(args[5]);
                    var r=independenceSupportService.transferResource(args[2],args[3],resource,amount);
                    flushEconomyState("independence-support-resource:"+r.supporterNationId()+":"+r.recipientNationId()+":"+r.resource());
                    sender.sendMessage("§a독립 준비 자원을 지원했습니다: §e"+r.supporterNationId()+" → "+r.recipientNationId()+" §7"+resourceKorean(r.resource())+" "+r.amount());
                    sender.sendMessage("§7지원국 잔량="+r.supporterAmount()+" / 수령국 보유량="+r.recipientAmount());
                    sender.sendMessage("§8이 지원은 독립전쟁을 자동으로 시작하거나 선언 조건을 우회하지 않습니다.");
                }
                case "tribute-status" -> { requireArgs(args,3,"/danta diplomacy tribute-status <속국-id>"); var r=vassalService.relation(args[2]).orElseThrow(()->new IllegalStateException("nation is not vassal")); sender.sendMessage("§6[속국 조공] §e"+args[2]+" → "+r.overlordNationId()); sender.sendMessage("§7국고 세입 조공률: §e"+vassalPolicyService.tributePercent()+"% §8(개발용 임시값, 기획 범위 10~25%)"); sender.sendMessage("§7개인지갑: §a조공 대상 아님"); }
                case "vassal-status" -> { requireArgs(args,3,"/danta diplomacy vassal-status <국가-id>"); var n=gameState.nation(args[2]).orElseThrow(()->new IllegalArgumentException("nation does not exist: "+args[2])); var r=vassalService.relation(args[2]).orElse(null); sender.sendMessage("§6[종속 상태] §e"+args[2]+" §7"+(r==null?(n.status()==kr.danta.core.nation.NationStatus.VASSAL?"속국(종주국 정보 없음)":"독립"):"속국 / 종주국="+r.overlordNationId())); }
                                case "npc-political" -> { requireArgs(args,4,"/danta diplomacy npc-political <npc국가-id> <ally|subjugate|annex|independent> [상대국-id]"); var action=args[3].toLowerCase(Locale.ROOT); if(action.equals("independent")){var s=npcNationService.state(args[2]).orElseThrow(()->new IllegalStateException("nation is not npc controlled"));s.restoreIndependent();sender.sendMessage("§aNPC 소국이 독립 상태로 복귀했습니다: §e"+args[2]);}else{requireArgs(args,5,"/danta diplomacy npc-political <npc국가-id> <ally|subjugate|annex> <상대국-id>");switch(action){case "ally"->{var s=npcPoliticalService.ally(args[2],args[4]);sender.sendMessage("§aNPC 소국과 동맹 관계를 수립했습니다: §e"+s.nationId()+" ↔ "+args[4]);}case "subjugate"->{var s=npcPoliticalService.subjugate(args[2],args[4]);sender.sendMessage("§aNPC 소국을 복속했습니다: §e"+s.nationId()+" §7종주국="+args[4]);}case "annex"->{var r=npcPoliticalService.annex(args[2],args[4]);sender.sendMessage("§aNPC 소국을 합병했습니다: §e"+r.npc().nationId()+" §7합병국="+args[4]+", 이전 거점="+r.transferredPoints()+"개");}default->throw new IllegalArgumentException("unknown npc political action");}} }
                case "npc-status" -> { requireArgs(args,3,"/danta diplomacy npc-status <npc국가-id>");var s=npcNationService.state(args[2]).orElseThrow(()->new IllegalStateException("nation is not npc controlled"));sender.sendMessage("§6[NPC 정치 상태] §e"+s.nationId()+" §7"+npcPoliticalKo(s.politicalStatus())+(s.patronNationId()==null?"":" / 상대국="+s.patronNationId())); }
                                case "npc-register" -> { requireArgs(args,3,"/danta diplomacy npc-register <국가-id>"); var state=npcNationService.register(args[2]); sender.sendMessage("§aNPC 국가로 등록했습니다: §e"+state.nationId()+" §7전략 AI 상태="+npcPhaseKo(state.phase())); }
                case "npc-unregister" -> { requireArgs(args,3,"/danta diplomacy npc-unregister <국가-id>"); npcNationService.unregister(args[2]); sender.sendMessage("§aNPC 국가 지정을 해제했습니다: §e"+args[2]); }
                case "npc-step" -> { requireArgs(args,4,"/danta diplomacy npc-step <국가-id> <evaluate|decide|execute|finish|cancel> [결정-key]"); var state=npcNationService.state(args[2]).orElseThrow(()->new IllegalStateException("nation is not npc controlled")); switch(args[3].toLowerCase(Locale.ROOT)){case "evaluate"->state.beginEvaluation();case "decide"->{requireArgs(args,5,"/danta diplomacy npc-step <국가-id> decide <결정-key>");state.decide(args[4]);}case "execute"->state.beginExecution();case "finish"->state.finishExecution();case "cancel"->state.cancel();default->throw new IllegalArgumentException("unknown npc step");} sender.sendMessage("§aNPC 전략 AI 상태를 변경했습니다: §e"+args[2]+" §7"+npcPhaseKo(state.phase())+(state.decisionKey()==null?"":" / 결정="+state.decisionKey())); }
                case "npc-list" -> { sender.sendMessage("§6[NPC 국가] §7총 "+npcNationService.states().size()+"개"); for(var state:npcNationService.states())sender.sendMessage("§e"+state.nationId()+" §7상태="+npcPhaseKo(state.phase())+(state.decisionKey()==null?"":" / 결정="+state.decisionKey())); }
                case "access" -> { requireArgs(args, 4, "/danta diplomacy access <이용국> <영토국>"); sender.sendMessage("§6[외교 이용권] §e"+args[2]+" → "+args[3]); sender.sendMessage("§7통행권: "+(diplomaticAccessService.hasRight(args[2],args[3],AccessRight.PASSAGE)?"§a허용":"§c불허")); sender.sendMessage("§7보급권: "+(diplomaticAccessService.hasRight(args[2],args[3],AccessRight.SUPPLY)?"§a허용":"§c불허")); }
                case "war" -> { requireArgs(args, 4, "/danta diplomacy war <공격국> <방어국>"); requireSeasonAction(SeasonAction.PLAYER_WAR); var war=warService.declareWar(args[2],args[3]); flushDiplomacyState("war-declare:"+war.warId()); sender.sendMessage("§c전쟁을 선언했습니다: §e"+war.initiatorNationId()+" → "+war.targetNationId()+" §7전쟁 ID="+war.warId()); sender.sendMessage("§7공격측="+String.join(", ",war.attackers())+" / 방어측="+String.join(", ",war.defenders())); }
                case "support" -> { requireArgs(args, 5, "/danta diplomacy support <전쟁-id> <국가-id> <attacker|defender>"); var war=warService.supportJoin(java.util.UUID.fromString(args[2]),args[3],WarSide.valueOf(args[4].toUpperCase(Locale.ROOT))); flushDiplomacyState("war-support:"+war.warId()+":"+args[3]); sender.sendMessage("§a공식 지원참전했습니다: §e"+args[3]+" §7"+(war.sideOf(args[3])==WarSide.ATTACKER?"공격측":"방어측")); }
                case "wars" -> { sender.sendMessage("§6[진행 중 전쟁] §7총 "+warService.wars().size()+"개"); for(var war:warService.wars()) sender.sendMessage("§e"+war.warId()+" §7공격측="+String.join(", ",war.attackers())+" / 방어측="+String.join(", ",war.defenders())); }
                case "war-end" -> { requireArgs(args,3,"/danta diplomacy war-end <전쟁-id>"); var warId=java.util.UUID.fromString(args[2]); warService.endWar(warId); flushDiplomacyState("war-end:"+warId); sender.sendMessage("§a전쟁을 종료했습니다: §e"+warId); }
                case "set" -> {
                    requireArgs(args, 5, "/danta diplomacy set <국가1> <국가2> <neutral|friendly|alliance|war>");
                    DiplomaticStatus status = DiplomaticStatus.valueOf(args[4].toUpperCase(Locale.ROOT));
                    if(status==DiplomaticStatus.ALLIANCE && ((!vassalPolicyService.canFormAlliance(args[2])) || (!vassalPolicyService.canFormAlliance(args[3])))) throw new IllegalStateException("vassal cannot form alliance");
                    var relation = diplomacyService.setStatus(args[2], args[3], status);
                    flushDiplomacyState("diplomacy-set:" + relation.nationAId() + ":" + relation.nationBId());
                    sender.sendMessage("§a외교 관계를 변경했습니다: §e" + relation.nationAId() + " ↔ " + relation.nationBId() + " §7" + diplomacyStatusKo(status));
                }
                case "show" -> { requireArgs(args, 4, "/danta diplomacy show <국가1> <국가2>"); sender.sendMessage("§6[외교 관계] §e"+args[2]+" ↔ "+args[3]+" §7"+diplomacyStatusKo(diplomacyService.status(args[2],args[3]))); }
                case "list" -> { sender.sendMessage("§6[외교 관계 목록] §7총 "+diplomacyService.relations().size()+"개"); for(var r:diplomacyService.relations()) sender.sendMessage("§e"+r.nationAId()+" ↔ "+r.nationBId()+" §7"+diplomacyStatusKo(r.status())); }
                default -> sender.sendMessage("§e/danta diplomacy <set|show|list|access|war|war-end|support|wars|npc-register|npc-unregister|npc-step|npc-list|npc-political|npc-status|vassalize|vassal-status|tribute-status|independence-declare|independence-status|independence-support-gold|independence-support-resource|independence-support-join>");
            }
        } catch (RuntimeException ex) { sendCommandError(sender, "외교", ex); }
        return true;
    }
    private static String npcPoliticalKo(kr.danta.core.npc.NpcPoliticalStatus s){return switch(s){case INDEPENDENT->"독립";case ALLIED->"동맹";case SUBJUGATED->"복속";case ANNEXED->"합병됨";};}
    private static String npcPhaseKo(kr.danta.core.npc.StrategicAiPhase p) { return switch(p){case IDLE->"대기";case EVALUATING->"상황 평가";case DECIDED->"행동 결정";case EXECUTING->"행동 실행";}; }
    private static String diplomacyStatusKo(DiplomaticStatus s) { return switch(s){case NEUTRAL->"중립";case FRIENDLY->"우호";case ALLIANCE->"혈맹";case WAR->"전쟁";}; }
    private void flushDiplomacyState(String reason) { if(snapshotService!=null && databaseService!=null && databaseService.health().status().name().equals("READY")) snapshotService.flushImportantAsync(reason); }

    private boolean handleResearch(CommandSender sender, String[] args) {
        if (!sender.hasPermission("danta.admin.point")) {
            sender.sendMessage("§c연구 관리 권한이 없습니다.");
            return true;
        }
        String sub = args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : "show";
        try {
            switch (sub) {
                case "reserve" -> {
                    requireArgs(args, 4, "/danta research reserve <국가-id> <연구-id>");
                    var entry = researchService.reserve(args[2], args[3]);
                    flushResearchState("research-reserve:" + entry.entryId());
                    sender.sendMessage("§a개발 검증용 연구를 예약했습니다: §e" + entry.researchId()
                            + " §7국가=" + args[2] + (entry.active() ? ", 즉시 연구 시작" : ", 대기열 등록"));
                    sender.sendMessage("§7연구 예약 ID: " + entry.entryId());
                }
                case "slots" -> {
                    requireArgs(args, 4, "/danta research slots <국가-id> <1|2>");
                    int slots = Integer.parseInt(args[3]);
                    researchService.setResearchSlots(args[2], slots);
                    flushResearchState("research-slots:" + args[2]);
                    sender.sendMessage("§a동시 연구 슬롯을 §e" + slots + "개§a로 설정했습니다.");
                }
                case "cancel" -> {
                    requireArgs(args, 4, "/danta research cancel <국가-id> <예약-id>");
                    var cancelled = researchService.cancel(args[2], UUID.fromString(args[3]));
                    flushResearchState("research-cancel:" + cancelled.entryId());
                    sender.sendMessage("§a연구 예약을 취소했습니다: §e" + cancelled.researchId());
                }
                case "show" -> {
                    requireArgs(args, 3, "/danta research show <국가-id>");
                    var state = researchService.state(args[2]);
                    sender.sendMessage("§6[국가 연구] §e" + state.nationId() + " §7동시 슬롯=" + state.researchSlots());
                    sender.sendMessage("§7완료 연구: " + (state.completed().isEmpty() ? "없음" : String.join(", ", state.completed())));
                    sender.sendMessage("§7연구 대기열: " + state.queue().size() + "개");
                    for (var entry : state.queue()) {
                        if (entry.active()) {
                            long remaining = Math.max(0L, entry.dueRuntimeMillis() - runtimeClock.elapsedMillis());
                            sender.sendMessage("§f- §e" + entry.researchId() + " §6[연구 중] §7남은 서버시간≈"
                                    + (remaining / 1000L) + "초, ID=" + entry.entryId());
                        } else {
                            sender.sendMessage("§f- §e" + entry.researchId() + " §7[예약 대기], ID=" + entry.entryId());
                        }
                    }
                }
                default -> sender.sendMessage("§e/danta research <reserve|slots|cancel|show>");
            }
        } catch (RuntimeException ex) {
            sendCommandError(sender, "연구", ex);
        }
        return true;
    }

    private void flushResearchState(String reason) {
        if (snapshotService == null || databaseService == null
                || !databaseService.health().status().name().equals("READY")) return;
        snapshotService.flushImportantAsync(reason);
    }

    private boolean handleFacility(CommandSender sender, String[] args) {
        if (!sender.hasPermission("danta.admin.point")) {
            sender.sendMessage("§c시설 관리 권한이 없습니다.");
            return true;
        }
        String sub = args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : "list";
        try {
            switch (sub) {
                case "build" -> {
                    requireArgs(args, 5, "/danta facility build <거점-id> <시설-id> <건설-초>");
                    long seconds = Long.parseLong(args[4]);
                    if (seconds < 0L || seconds > 86_400L) throw new IllegalArgumentException("construction seconds must be 0..86400");
                    var pending = facilityConstructionService.scheduleBuild(args[2], args[3], Duration.ofSeconds(seconds));
                    flushFacilityState("facility-construction-schedule:" + pending.constructionId());
                    sender.sendMessage("§a개발 검증용 시설 건설을 예약했습니다: §e" + args[3]
                            + " §7거점=" + args[2] + ", 목표=I, 남은 서버시간≈" + seconds + "초");
                    sender.sendMessage("§7건설 ID: " + pending.constructionId());
                }
                case "upgrade" -> {
                    requireArgs(args, 5, "/danta facility upgrade <거점-id> <시설-id> <건설-초>");
                    long seconds = Long.parseLong(args[4]);
                    if (seconds < 0L || seconds > 86_400L) throw new IllegalArgumentException("construction seconds must be 0..86400");
                    var pending = facilityConstructionService.scheduleUpgrade(args[2], args[3], Duration.ofSeconds(seconds));
                    flushFacilityState("facility-construction-upgrade:" + pending.constructionId());
                    sender.sendMessage("§a개발 검증용 시설 업그레이드를 예약했습니다: §e" + args[3]
                            + " §7거점=" + args[2] + ", 목표=" + pending.targetTier() + ", 남은 서버시간≈" + seconds + "초");
                    sender.sendMessage("§7건설 ID: " + pending.constructionId());
                }
                case "list" -> {
                    sender.sendMessage("§6[시설 건설 대기 목록] §7총 " + facilityConstructionService.pending().size() + "개");
                    for (var pending : facilityConstructionService.pending()) {
                        long remaining = Math.max(0L, pending.dueRuntimeMillis() - runtimeClock.elapsedMillis());
                        sender.sendMessage("§e" + pending.facilityId() + " §7거점=" + pending.pointId()
                                + ", 목표=" + pending.targetTier() + ", 남은 서버시간≈" + (remaining / 1000L) + "초"
                                + ", ID=" + pending.constructionId());
                    }
                }
                case "visual-sync" -> {
                    requireArgs(args, 4, "/danta facility visual-sync <거점-id> <시설-id>");
                    var result = facilityAppearanceService.sync(args[2], args[3]);
                    switch (result) {
                        case SYNCED -> sender.sendMessage("§a시설 외형을 동기화했습니다.");
                        case CHUNK_UNLOADED -> sender.sendMessage("§e대상 청크가 로드되지 않아 외형 동기화를 대기열에 등록했습니다.");
                        case TEMPLATE_MISSING -> sender.sendMessage("§e해당 등급의 NBT 구조물 파일이 없어 외형 동기화를 대기합니다: §7"
                                + facilityAppearanceService.templateKey(facilityService.facility(args[2], args[3]).orElseThrow()));
                        case WORLD_UNAVAILABLE -> sender.sendMessage("§e대상 월드가 로드되지 않아 외형 동기화를 대기열에 등록했습니다.");
                    }
                }
                case "visual-status" -> sender.sendMessage("§6[시설 외형 동기화] §7대기=" + facilityAppearanceService.pendingCount() + "개");
                case "show" -> {
                    requireArgs(args, 3, "/danta facility show <거점-id>");
                    requireStrategicPoint(args[2]);
                    var built = facilityService.facilities(args[2]);
                    sender.sendMessage("§6[거점 시설] §e" + args[2] + " §7완성=" + built.size() + "개");
                    for (var facility : built) sender.sendMessage("§f- §e" + facility.facilityId() + " §7등급=" + facility.tier());
                    for (var pending : facilityConstructionService.pending().stream().filter(x -> x.pointId().equals(args[2])).toList()) {
                        long remaining = Math.max(0L, pending.dueRuntimeMillis() - runtimeClock.elapsedMillis());
                        sender.sendMessage("§f- §e" + pending.facilityId() + " §6[건설 중] §7목표=" + pending.targetTier()
                                + ", 남은 서버시간≈" + (remaining / 1000L) + "초");
                    }
                }
                default -> sender.sendMessage("§e/danta facility <build|upgrade|list|show|visual-sync|visual-status>");
            }
        } catch (RuntimeException ex) {
            sendCommandError(sender, "시설", ex);
        }
        return true;
    }

    private void flushFacilityState(String reason) {
        if (snapshotService == null || databaseService == null
                || !databaseService.health().status().name().equals("READY")) return;
        snapshotService.flushImportantAsync(reason);
    }

}
