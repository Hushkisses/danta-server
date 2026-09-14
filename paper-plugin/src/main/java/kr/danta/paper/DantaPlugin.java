package kr.danta.paper;

import kr.danta.core.DantaCore;
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
import kr.danta.core.state.GameState;
import kr.danta.core.territory.BattlefieldTag;
import kr.danta.core.territory.PointPosition;
import kr.danta.core.territory.StrategicEdge;
import kr.danta.core.territory.StrategicPoint;
import kr.danta.core.territory.StrategicPointType;
import kr.danta.core.territory.TerritoryService;
import kr.danta.core.territory.event.StrategicPointOwnershipChangedEvent;
import kr.danta.paper.persistence.DatabaseConfig;
import kr.danta.paper.persistence.DatabaseConfigLoader;
import kr.danta.paper.persistence.DatabaseHealth;
import kr.danta.paper.persistence.PostgresDatabaseService;
import kr.danta.paper.persistence.PostgresKeyValueRepository;
import kr.danta.paper.persistence.SnapshotService;
import kr.danta.paper.runtime.PropertiesRuntimeClockRepository;
import kr.danta.paper.ui.NationGuiController;
import kr.danta.paper.ui.MapGuiController;
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
    private TerritoryService territoryService;
    private NationGuiController nationGuiController;
    private MapGuiController mapGuiController;
    private BukkitTask runtimeSchedulerPump;

    private PostgresDatabaseService databaseService;
    private AsyncKeyValueRepository devRepository;
    private SnapshotService snapshotService;
    private BukkitTask snapshotTask;

    @Override public void onEnable() {
        gameState = new GameState();
        eventBus = new DomainEventBus();
        territoryService = new TerritoryService(gameState, eventBus);
        nationGuiController = new NationGuiController(gameState);
        getServer().getPluginManager().registerEvents(nationGuiController, this);
        mapGuiController = new MapGuiController(gameState);
        getServer().getPluginManager().registerEvents(mapGuiController, this);
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

        runtimeScheduler = new RuntimeScheduler(runtimeClock);
        runtimeScheduler.registerHandler("dev.echo", task ->
                getLogger().info("[RuntimeScheduler] dev.echo completed: "
                        + task.payload().getOrDefault("message", "(no message)")
                        + " [id=" + task.id() + "]"));
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
        if (args.length > 0 && args[0].equalsIgnoreCase("db")) return handleDatabase(sender, args);
        if (args.length > 0 && args[0].equalsIgnoreCase("snapshot")) return handleSnapshot(sender, args);
        if (args.length > 0 && args[0].equalsIgnoreCase("nation")) return handleNation(sender, args);
        if (args.length > 0 && args[0].equalsIgnoreCase("point")) return handleStrategicPoint(sender, args);
        if (args.length > 0 && args[0].equalsIgnoreCase("edge")) return handleStrategicEdge(sender, args);

        sender.sendMessage("§6[Danta Server DEV]");
        sender.sendMessage("§fPlugin: §e" + getPluginMeta().getVersion());
        sender.sendMessage("§fCore: §e" + DantaCore.VERSION);
        sender.sendMessage("§fMinecraft/Paper target: §e26.2 / build 123");
        sender.sendMessage("§fJava target: §e25");
        sender.sendMessage("§fRuntime: §e" + formatRuntime(runtimeClock.elapsedMillis())
                + (runtimeClock.isPaused() ? " §c[PAUSED]" : "") + " §7x" + runtimeClock.speedMultiplier());
        sender.sendMessage("§7DEV-015 scheduler queued=" + runtimeScheduler.size());
        if (databaseService == null) {
            sender.sendMessage("§7DEV-016 DB: §cCONFIG ERROR");
        } else {
            sender.sendMessage("§7DEV-016 DB: §e" + databaseService.health().status());
        }
        return true;
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
            sender.sendMessage("§cThe nation GUI can only be opened by a player.");
            return true;
        }
        String nationId = args.length >= 1 ? args[0] : null;
        try {
            nationGuiController.open(player, nationId);
        } catch (RuntimeException ex) {
            sender.sendMessage("§cNation GUI failed: " + ex.getMessage());
        }
        return true;
    }


    private boolean handleStrategicEdge(CommandSender sender, String[] args) {
        if (!sender.hasPermission("danta.admin.edge")) {
            sender.sendMessage("§cNo permission: danta.admin.edge");
            return true;
        }
        String sub = args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : "list";
        try {
            switch (sub) {
                case "create" -> {
                    requireArgs(args, 7, "/danta edge create <id> <point-a> <point-b> <travel-seconds> <tag[,tag...]|none>");
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
                    sender.sendMessage("§aStrategic edge created: §e" + id + " §f(" + pointA + " <-> " + pointB + ")");
                }
                case "list" -> {
                    sender.sendMessage("§6[Danta Strategic Edges] §7count=" + gameState.strategicEdges().size());
                    for (StrategicEdge edge : gameState.strategicEdges()) {
                        sender.sendMessage("§e" + edge.edgeId() + " §f" + edge.pointAId() + " <-> " + edge.pointBId()
                                + " §7travel=" + formatTravel(edge.baseTravelMillis()) + ", tags=" + edge.battlefieldTags());
                    }
                }
                case "show" -> {
                    requireArgs(args, 3, "/danta edge show <id>");
                    sendStrategicEdge(sender, requireStrategicEdge(args[2]));
                }
                case "travel" -> {
                    requireArgs(args, 4, "/danta edge travel <id> <seconds>");
                    StrategicEdge edge = requireStrategicEdge(args[2]);
                    long seconds = Long.parseLong(args[3]);
                    if (seconds <= 0 || seconds > 86_400L) throw new IllegalArgumentException("seconds must be 1..86400");
                    edge.setBaseTravelMillis(Math.multiplyExact(seconds, 1000L));
                    flushStrategicEdgeState("edge-travel:" + edge.edgeId());
                    sender.sendMessage("§aTravel time updated: §e" + formatTravel(edge.baseTravelMillis()));
                }
                case "tags" -> {
                    requireArgs(args, 4, "/danta edge tags <id> <tag[,tag...]|none>");
                    StrategicEdge edge = requireStrategicEdge(args[2]);
                    edge.setBattlefieldTags(parseBattlefieldTags(args[3]));
                    flushStrategicEdgeState("edge-tags:" + edge.edgeId());
                    sender.sendMessage("§aBattlefield tags updated: §e" + edge.battlefieldTags());
                }
                case "neighbors" -> {
                    requireArgs(args, 3, "/danta edge neighbors <point-id>");
                    StrategicPoint point = requireStrategicPoint(args[2]);
                    var edges = gameState.edgesForPoint(point.pointId());
                    sender.sendMessage("§6[Neighbors] §e" + point.pointId() + " §7count=" + edges.size());
                    for (StrategicEdge edge : edges) {
                        sender.sendMessage("§f- §e" + edge.otherPoint(point.pointId()) + " §7via=" + edge.edgeId()
                                + ", travel=" + formatTravel(edge.baseTravelMillis()) + ", tags=" + edge.battlefieldTags());
                    }
                }
                default -> sender.sendMessage("§e/danta edge <create|list|show|travel|tags|neighbors>");
            }
        } catch (RuntimeException ex) {
            sender.sendMessage("§cStrategic edge command failed: " + ex.getMessage());
        }
        return true;
    }

    private StrategicEdge requireStrategicEdge(String edgeId) {
        return gameState.strategicEdge(edgeId)
                .orElseThrow(() -> new IllegalArgumentException("strategic edge not found: " + edgeId));
    }

    private void sendStrategicEdge(CommandSender sender, StrategicEdge edge) {
        sender.sendMessage("§6[Danta Strategic Edge] §e" + edge.edgeId());
        sender.sendMessage("§fEndpoints: §e" + edge.pointAId() + " §f<-> §e" + edge.pointBId());
        sender.sendMessage("§fBase travel: §e" + formatTravel(edge.baseTravelMillis()));
        sender.sendMessage("§fBattlefield tags: §e" + edge.battlefieldTags());
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
        return minutes + "m " + seconds + "s";
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
            sender.sendMessage("§cNo permission: danta.admin.point");
            return true;
        }
        String sub = args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : "list";
        try {
            switch (sub) {
                case "create" -> {
                    requireArgs(args, 10, "/danta point create <id> <type> <world> <x> <y> <z> <slots> <display name>");
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
                    sender.sendMessage("§aStrategic point created: §e" + id + " §f(" + name + ")");
                }
                case "list" -> {
                    sender.sendMessage("§6[Danta Strategic Points] §7count=" + gameState.strategicPoints().size());
                    for (StrategicPoint point : gameState.strategicPoints()) {
                        PointPosition pos = point.position();
                        sender.sendMessage("§e" + point.pointId() + " §f" + point.displayName()
                                + " §7type=" + point.type() + ", owner=" + point.ownerNationId().orElse("-")
                                + ", pos=" + pos.worldName() + ":" + pos.x() + "," + pos.y() + "," + pos.z());
                    }
                }
                case "show" -> {
                    requireArgs(args, 3, "/danta point show <id>");
                    sendStrategicPoint(sender, requireStrategicPoint(args[2]));
                }
                case "owner" -> {
                    requireArgs(args, 4, "/danta point owner <id> <nation-id|none>");
                    TerritoryService.OwnershipChangeResult result = territoryService.changeOwner(
                            args[2], args[3], "admin-command");
                    if (result.changed()) {
                        sender.sendMessage("§aOwner updated: §e"
                                + (result.previousOwnerNationId() == null ? "none" : result.previousOwnerNationId())
                                + " §f-> §e"
                                + (result.newOwnerNationId() == null ? "none" : result.newOwnerNationId()));
                    } else {
                        sender.sendMessage("§7Owner unchanged: §e"
                                + (result.newOwnerNationId() == null ? "none" : result.newOwnerNationId()));
                    }
                }
                case "production" -> {
                    requireArgs(args, 5, "/danta point production <id> <resource-key> <per-hour>");
                    StrategicPoint point = requireStrategicPoint(args[2]);
                    point.setBaseProduction(args[3], Long.parseLong(args[4]));
                    flushStrategicPointState("point-production:" + point.pointId());
                    sender.sendMessage("§aProduction updated: §e" + args[3].toLowerCase(Locale.ROOT)
                            + "=" + point.baseProductionPerHour().getOrDefault(args[3].toLowerCase(Locale.ROOT), 0L) + "/h");
                }
                case "slots" -> {
                    requireArgs(args, 4, "/danta point slots <id> <0-16>");
                    StrategicPoint point = requireStrategicPoint(args[2]);
                    point.setFacilitySlots(Integer.parseInt(args[3]));
                    flushStrategicPointState("point-slots:" + point.pointId());
                    sender.sendMessage("§aFacility slots updated: §e" + point.facilitySlots());
                }
                default -> sender.sendMessage("§e/danta point <create|list|show|owner|production|slots>");
            }
        } catch (RuntimeException ex) {
            sender.sendMessage("§cStrategic point command failed: " + ex.getMessage());
        }
        return true;
    }

    private StrategicPoint requireStrategicPoint(String pointId) {
        return gameState.strategicPoint(pointId)
                .orElseThrow(() -> new IllegalArgumentException("strategic point not found: " + pointId));
    }

    private void sendStrategicPoint(CommandSender sender, StrategicPoint point) {
        PointPosition pos = point.position();
        sender.sendMessage("§6[Danta Strategic Point] §e" + point.pointId());
        sender.sendMessage("§fName: §e" + point.displayName());
        sender.sendMessage("§fType: §e" + point.type());
        sender.sendMessage("§fOwner: §e" + point.ownerNationId().orElse("none"));
        sender.sendMessage("§fPosition: §e" + pos.worldName() + " " + pos.x() + " " + pos.y() + " " + pos.z());
        sender.sendMessage("§fFacility slots: §e" + point.facilitySlots());
        sender.sendMessage("§fBase production/h: §e" + point.baseProductionPerHour());
    }

    private void flushStrategicPointState(String reason) {
        if (snapshotService == null || databaseService == null) return;
        if (!databaseService.health().status().name().equals("READY")) return;
        snapshotService.flushImportantAsync(reason).whenComplete((ignored, error) -> {
            if (error != null) getLogger().warning("DEV-021 strategic point snapshot flush failed: " + rootMessage(error));
        });
    }

    private boolean handleNation(CommandSender sender, String[] args) {
        if (!sender.hasPermission("danta.admin.nation")) {
            sender.sendMessage("§cNo permission: danta.admin.nation");
            return true;
        }
        String sub = args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : "list";
        try {
            switch (sub) {
                case "create" -> {
                    requireArgs(args, 4, "/danta nation create <id> <display name>");
                    String id = args[2];
                    String name = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                    NationState nation = new NationState(id, name);
                    gameState.addNation(nation);
                    flushNationState("nation-create:" + id);
                    sender.sendMessage("§aNation created: §e" + id + " §f(" + name + ")");
                }
                case "list" -> {
                    sender.sendMessage("§6[Danta Nations] §7count=" + gameState.nations().size());
                    for (NationState nation : gameState.nations()) {
                        sender.sendMessage("§e" + nation.nationId() + " §f" + nation.displayName()
                                + " §7capital=" + nation.capitalPointId().orElse("-")
                                + ", treasury=" + nation.treasury() + "G, status=" + nation.status());
                    }
                }
                case "show" -> {
                    requireArgs(args, 3, "/danta nation show <id>");
                    sendNation(sender, requireNation(args[2]));
                }
                case "capital" -> {
                    requireArgs(args, 4, "/danta nation capital <id> <point-id|none>");
                    NationState nation = requireNation(args[2]);
                    String capitalPointId = args[3].equalsIgnoreCase("none") ? null : args[3];
                    if (capitalPointId != null) requireStrategicPoint(capitalPointId);
                    nation.setCapitalPointId(capitalPointId);
                    flushNationState("nation-capital:" + nation.nationId());
                    sender.sendMessage("§aCapital updated: §e" + nation.capitalPointId().orElse("none"));
                }
                case "treasury" -> {
                    requireArgs(args, 4, "/danta nation treasury <id> <gold>");
                    NationState nation = requireNation(args[2]);
                    nation.setTreasury(Long.parseLong(args[3]));
                    flushNationState("nation-treasury:" + nation.nationId());
                    sender.sendMessage("§aTreasury updated: §e" + nation.treasury() + "G");
                }
                case "status" -> {
                    requireArgs(args, 4, "/danta nation status <id> <ACTIVE|VASSAL>");
                    NationState nation = requireNation(args[2]);
                    nation.setStatus(NationStatus.valueOf(args[3].toUpperCase(Locale.ROOT)));
                    flushNationState("nation-status:" + nation.nationId());
                    sender.sendMessage("§aNation status updated: §e" + nation.status());
                }
                default -> sender.sendMessage("§e/danta nation <create|list|show|capital|treasury|status>");
            }
        } catch (RuntimeException ex) {
            sender.sendMessage("§cNation command failed: " + ex.getMessage());
        }
        return true;
    }

    private NationState requireNation(String nationId) {
        return gameState.nation(nationId)
                .orElseThrow(() -> new IllegalArgumentException("nation not found: " + nationId));
    }

    private void sendNation(CommandSender sender, NationState nation) {
        sender.sendMessage("§6[Danta Nation] §e" + nation.nationId());
        sender.sendMessage("§fName: §e" + nation.displayName());
        sender.sendMessage("§fCapital: §e" + nation.capitalPointId().orElse("-"));
        sender.sendMessage("§fTreasury: §e" + nation.treasury() + "G");
        sender.sendMessage("§fStatus: §e" + nation.status());
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
            sender.sendMessage("§cNo permission: danta.admin.db");
            return true;
        }
        if (snapshotService == null || databaseService == null || databaseService.health().status().name().equals("DISABLED")) {
            sender.sendMessage("§cSnapshot service requires enabled PostgreSQL.");
            return true;
        }
        String sub = args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : "save";
        switch (sub) {
            case "save" -> {
                sender.sendMessage("§eSaving snapshot asynchronously...");
                snapshotService.saveAsync("manual").whenComplete((ignored, error) -> runSync(() ->
                        sender.sendMessage(error == null ? "§aSnapshot saved." : "§cSnapshot failed: " + rootMessage(error))));
            }
            case "important" -> {
                String reason = args.length >= 3 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "manual-test";
                sender.sendMessage("§eFlushing important snapshot asynchronously...");
                snapshotService.flushImportantAsync(reason).whenComplete((ignored, error) -> runSync(() ->
                        sender.sendMessage(error == null ? "§aImportant flush complete." : "§cImportant flush failed: " + rootMessage(error))));
            }
            case "load" -> {
                sender.sendMessage("§eLoading snapshot asynchronously...");
                snapshotService.loadAsync().whenComplete((snapshot, error) -> runSync(() -> {
                    if (error != null) sender.sendMessage("§cSnapshot load failed: " + rootMessage(error));
                    else if (snapshot.isEmpty()) sender.sendMessage("§7No snapshot exists.");
                    else {
                        snapshotService.apply(snapshot.get());
                        persistRuntime();
                        sender.sendMessage("§aSnapshot restored. Runtime=§e" + formatRuntime(runtimeClock.elapsedMillis()));
                    }
                }));
            }
            default -> sender.sendMessage("§e/danta snapshot <save|important [reason]|load>");
        }
        return true;
    }
    private boolean handleDatabase(CommandSender sender, String[] args) {
        if (!sender.hasPermission("danta.admin.db")) {
            sender.sendMessage("§cNo permission: danta.admin.db");
            return true;
        }
        if (databaseService == null) {
            sender.sendMessage("§cDatabase service was not initialized. Check server console/config.");
            return true;
        }

        String sub = args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : "status";
        try {
            switch (sub) {
                case "status" -> sendDatabaseStatus(sender);
                case "reconnect" -> {
                    sender.sendMessage("§eStarting asynchronous PostgreSQL connection test...");
                    databaseService.initializeAsync().whenComplete((ready, error) -> runSync(() -> {
                        if (error != null || !Boolean.TRUE.equals(ready)) {
                            sender.sendMessage("§cPostgreSQL connection failed. /danta db status");
                        } else {
                            sender.sendMessage("§aPostgreSQL READY.");
                        }
                    }));
                }
                case "ping" -> {
                    sender.sendMessage("§ePinging PostgreSQL asynchronously...");
                    databaseService.pingAsync().whenComplete((ok, error) -> runSync(() -> {
                        if (error != null) sender.sendMessage("§cDB ping failed: " + rootMessage(error));
                        else sender.sendMessage(Boolean.TRUE.equals(ok) ? "§aDB ping: OK" : "§cDB ping: unexpected result");
                    }));
                }
                case "put" -> {
                    requireArgs(args, 4, "/danta db put <key> <value>");
                    String key = args[2];
                    String value = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                    devRepository.save("dev016", key, value).whenComplete((ignored, error) -> runSync(() -> {
                        if (error != null) sender.sendMessage("§cDB write failed: " + rootMessage(error));
                        else sender.sendMessage("§aSaved asynchronously: §e" + key);
                    }));
                }
                case "get" -> {
                    requireArgs(args, 3, "/danta db get <key>");
                    String key = args[2];
                    devRepository.find("dev016", key).whenComplete((value, error) -> runSync(() -> {
                        if (error != null) sender.sendMessage("§cDB read failed: " + rootMessage(error));
                        else sender.sendMessage(value.map(v -> "§a" + key + " = §e" + v)
                                .orElse("§7No value for key: " + key));
                    }));
                }
                case "delete" -> {
                    requireArgs(args, 3, "/danta db delete <key>");
                    String key = args[2];
                    devRepository.delete("dev016", key).whenComplete((deleted, error) -> runSync(() -> {
                        if (error != null) sender.sendMessage("§cDB delete failed: " + rootMessage(error));
                        else sender.sendMessage(Boolean.TRUE.equals(deleted)
                                ? "§aDeleted: §e" + key
                                : "§7Nothing to delete: " + key);
                    }));
                }
                default -> sender.sendMessage("§e/danta db <status|reconnect|ping|put|get|delete>");
            }
        } catch (RuntimeException ex) {
            sender.sendMessage("§cDatabase command failed: " + ex.getMessage());
        }
        return true;
    }

    private void sendDatabaseStatus(CommandSender sender) {
        DatabaseHealth health = databaseService.health();
        sender.sendMessage("§6[Danta PostgreSQL]");
        sender.sendMessage("§fStatus: §e" + health.status());
        sender.sendMessage("§fTarget: §e" + health.target());
        if (health.lastError() != null) sender.sendMessage("§fLast error: §c" + health.lastError());
        sender.sendMessage("§7Config: plugins/DantaServer/database.properties");
    }

    private boolean handleRuntime(CommandSender sender, String[] args) {
        if (!sender.hasPermission("danta.admin.runtime")) {
            sender.sendMessage("§cNo permission: danta.admin.runtime"); return true;
        }
        if (args.length == 1 || args[1].equalsIgnoreCase("status")) {
            sender.sendMessage("§6[Danta Runtime] §e" + formatRuntime(runtimeClock.elapsedMillis())
                    + " §7paused=" + runtimeClock.isPaused() + ", speed=x" + runtimeClock.speedMultiplier());
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
                    requireArgs(args, 3, "/danta runtime set <seconds>");
                    long seconds = Long.parseLong(args[2]);
                    if (seconds < 0 || seconds > Long.MAX_VALUE / 1000L) throw new IllegalArgumentException("invalid seconds");
                    runtimeClock.setElapsedMillis(seconds * 1000L);
                }
                case "save" -> persistRuntime();
                case "schedule" -> {
                    requireArgs(args, 3, "/danta runtime schedule <runtime-seconds> [message]");
                    long seconds = Long.parseLong(args[2]);
                    if (seconds < 0) throw new IllegalArgumentException("runtime-seconds must be >= 0");
                    String message = args.length >= 4
                            ? String.join(" ", Arrays.copyOfRange(args, 3, args.length))
                            : "DEV-015 test";
                    RuntimeScheduledTask task = runtimeScheduler.scheduleAfter(
                            Duration.ofSeconds(seconds), "dev.echo", Map.of("message", message));
                    sender.sendMessage("§aScheduled: §e" + task.id()
                            + " §7dueRuntime=" + formatRuntime(task.dueRuntimeMillis()));
                    return true;
                }
                case "scheduler" -> {
                    sender.sendMessage("§6[Danta Runtime Scheduler] §equeued=" + runtimeScheduler.size());
                    runtimeScheduler.nextTask().ifPresentOrElse(
                            task -> sender.sendMessage("§fNext: §e" + task.id()
                                    + " §7type=" + task.taskType()
                                    + ", due=" + formatRuntime(task.dueRuntimeMillis())),
                            () -> sender.sendMessage("§7No queued runtime tasks."));
                    return true;
                }
                case "cancel" -> {
                    requireArgs(args, 3, "/danta runtime cancel <task-uuid>");
                    UUID taskId = UUID.fromString(args[2]);
                    sender.sendMessage(runtimeScheduler.cancel(taskId)
                            ? "§aCancelled runtime task: §e" + taskId
                            : "§cRuntime task not found: " + taskId);
                    return true;
                }
                default -> {
                    sender.sendMessage("§e/danta runtime <status|pause|resume|speed|set|save|schedule|scheduler|cancel>");
                    return true;
                }
            }
            persistRuntime();
            sender.sendMessage("§aRuntime updated: §e" + formatRuntime(runtimeClock.elapsedMillis())
                    + " §7paused=" + runtimeClock.isPaused() + ", speed=x" + runtimeClock.speedMultiplier());
        } catch (RuntimeException ex) {
            sender.sendMessage("§cRuntime command failed: " + ex.getMessage());
        }
        return true;
    }

    private void pumpRuntimeScheduler() {
        if (runtimeScheduler == null) return;
        for (RuntimeTaskExecution execution : runtimeScheduler.executeDueTasks()) {
            if (!execution.success()) {
                getLogger().severe("Runtime task failed: id=" + execution.task().id()
                        + ", type=" + execution.task().taskType()
                        + ", error=" + execution.error().getMessage());
            }
        }
    }

    private void runSync(Runnable action) {
        if (!isEnabled()) return;
        getServer().getScheduler().runTask(this, action);
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
}
