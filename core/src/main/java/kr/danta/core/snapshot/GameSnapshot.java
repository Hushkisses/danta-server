package kr.danta.core.snapshot;

import java.util.List;

/** Restart-recovery snapshot. Active army movements: schema v6 (DEV-033). */
public record GameSnapshot(
        int schemaVersion,
        long createdAtEpochMillis,
        long runtimeElapsedMillis,
        boolean runtimePaused,
        double runtimeSpeedMultiplier,
        String seasonId,
        String seasonDisplayName,
        List<NationSnapshot> nations,
        List<StrategicPointSnapshot> strategicPoints,
        List<StrategicEdgeSnapshot> strategicEdges,
        List<ArmySnapshot> armies,
        List<ArmyOrderSnapshot> armyOrders,
        List<ArmyOperationQueueSnapshot> armyOperationQueues,
        List<PersonalWalletSnapshot> personalWallets,
        List<StrategicResourceStockpileSnapshot> strategicResourceStockpiles,
        List<LocalResourceStockpileSnapshot> localResourceStockpiles,
        List<GeneralSnapshot> generals
) {
    public static final int CURRENT_SCHEMA = 12;

    public GameSnapshot {
        if (schemaVersion <= 0) throw new IllegalArgumentException("schemaVersion must be positive");
        if (createdAtEpochMillis < 0 || runtimeElapsedMillis < 0) throw new IllegalArgumentException("negative time");
        if (!Double.isFinite(runtimeSpeedMultiplier) || runtimeSpeedMultiplier <= 0.0) {
            throw new IllegalArgumentException("invalid runtimeSpeedMultiplier");
        }
        if ((seasonId == null) != (seasonDisplayName == null)) {
            throw new IllegalArgumentException("season fields must both be null or non-null");
        }
        nations = nations == null ? List.of() : List.copyOf(nations);
        strategicPoints = strategicPoints == null ? List.of() : List.copyOf(strategicPoints);
        strategicEdges = strategicEdges == null ? List.of() : List.copyOf(strategicEdges);
        armies = armies == null ? List.of() : List.copyOf(armies);
        armyOrders = armyOrders == null ? List.of() : List.copyOf(armyOrders);
        armyOperationQueues = armyOperationQueues == null ? List.of() : List.copyOf(armyOperationQueues);
        personalWallets = personalWallets == null ? List.of() : List.copyOf(personalWallets);
        strategicResourceStockpiles = strategicResourceStockpiles == null ? List.of() : List.copyOf(strategicResourceStockpiles);
        localResourceStockpiles = localResourceStockpiles == null ? List.of() : List.copyOf(localResourceStockpiles);
        generals = generals == null ? List.of() : List.copyOf(generals);
    }

    /** Backward source-compatible DEV-030 constructor: armies existed before orders/queues. */
    public GameSnapshot(int schemaVersion, long createdAtEpochMillis, long runtimeElapsedMillis,
                        boolean runtimePaused, double runtimeSpeedMultiplier, String seasonId,
                        String seasonDisplayName, List<NationSnapshot> nations,
                        List<StrategicPointSnapshot> strategicPoints, List<StrategicEdgeSnapshot> strategicEdges,
                        List<ArmySnapshot> armies) {
        this(schemaVersion, createdAtEpochMillis, runtimeElapsedMillis, runtimePaused, runtimeSpeedMultiplier,
                seasonId, seasonDisplayName, nations, strategicPoints, strategicEdges, armies,
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    }

    /** Backward source-compatible constructor used by pre-DEV-050 callers/tests. */
    public GameSnapshot(int schemaVersion, long createdAtEpochMillis, long runtimeElapsedMillis,
                        boolean runtimePaused, double runtimeSpeedMultiplier, String seasonId,
                        String seasonDisplayName, List<NationSnapshot> nations,
                        List<StrategicPointSnapshot> strategicPoints, List<StrategicEdgeSnapshot> strategicEdges,
                        List<ArmySnapshot> armies, List<ArmyOrderSnapshot> armyOrders,
                        List<ArmyOperationQueueSnapshot> armyOperationQueues) {
        this(schemaVersion, createdAtEpochMillis, runtimeElapsedMillis, runtimePaused, runtimeSpeedMultiplier,
                seasonId, seasonDisplayName, nations, strategicPoints, strategicEdges, armies, armyOrders,
                armyOperationQueues, List.of(), List.of(), List.of(), List.of());
    }
    /** Backward source-compatible DEV-050 constructor with wallets but no strategic resources. */
    public GameSnapshot(int schemaVersion, long createdAtEpochMillis, long runtimeElapsedMillis,
                        boolean runtimePaused, double runtimeSpeedMultiplier, String seasonId,
                        String seasonDisplayName, List<NationSnapshot> nations,
                        List<StrategicPointSnapshot> strategicPoints, List<StrategicEdgeSnapshot> strategicEdges,
                        List<ArmySnapshot> armies, List<ArmyOrderSnapshot> armyOrders,
                        List<ArmyOperationQueueSnapshot> armyOperationQueues, List<PersonalWalletSnapshot> personalWallets) {
        this(schemaVersion, createdAtEpochMillis, runtimeElapsedMillis, runtimePaused, runtimeSpeedMultiplier,
                seasonId, seasonDisplayName, nations, strategicPoints, strategicEdges, armies, armyOrders,
                armyOperationQueues, personalWallets, List.of(), List.of(), List.of());
    }

    /** Backward source-compatible DEV-051 constructor without local stockpiles. */
    public GameSnapshot(int schemaVersion, long createdAtEpochMillis, long runtimeElapsedMillis,
                        boolean runtimePaused, double runtimeSpeedMultiplier, String seasonId,
                        String seasonDisplayName, List<NationSnapshot> nations,
                        List<StrategicPointSnapshot> strategicPoints, List<StrategicEdgeSnapshot> strategicEdges,
                        List<ArmySnapshot> armies, List<ArmyOrderSnapshot> armyOrders,
                        List<ArmyOperationQueueSnapshot> armyOperationQueues, List<PersonalWalletSnapshot> personalWallets,
                        List<StrategicResourceStockpileSnapshot> strategicResourceStockpiles) {
        this(schemaVersion, createdAtEpochMillis, runtimeElapsedMillis, runtimePaused, runtimeSpeedMultiplier,
                seasonId, seasonDisplayName, nations, strategicPoints, strategicEdges, armies, armyOrders,
                armyOperationQueues, personalWallets, strategicResourceStockpiles, List.of(), List.of());
    }

}
