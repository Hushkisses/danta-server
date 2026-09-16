package kr.danta.core.snapshot;

import java.util.List;

/** Restart-recovery snapshot. DEV-086 schema v15 adds national doctrine slot/selection state. */
public record GameSnapshot(
        int schemaVersion, long createdAtEpochMillis, long runtimeElapsedMillis, boolean runtimePaused,
        double runtimeSpeedMultiplier, String seasonId, String seasonDisplayName,
        List<NationSnapshot> nations, List<StrategicPointSnapshot> strategicPoints, List<StrategicEdgeSnapshot> strategicEdges,
        List<ArmySnapshot> armies, List<ArmyOrderSnapshot> armyOrders, List<ArmyOperationQueueSnapshot> armyOperationQueues,
        List<PersonalWalletSnapshot> personalWallets, List<StrategicResourceStockpileSnapshot> strategicResourceStockpiles,
        List<LocalResourceStockpileSnapshot> localResourceStockpiles, List<GeneralSnapshot> generals,
        List<FacilitySnapshot> facilities, List<FacilityConstructionSnapshot> facilityConstructions,
        List<ResearchStateSnapshot> researchStates
) {
    public static final int CURRENT_SCHEMA = 15;

    public GameSnapshot {
        if (schemaVersion <= 0) throw new IllegalArgumentException("schemaVersion must be positive");
        if (createdAtEpochMillis < 0 || runtimeElapsedMillis < 0) throw new IllegalArgumentException("negative time");
        if (!Double.isFinite(runtimeSpeedMultiplier) || runtimeSpeedMultiplier <= 0.0) throw new IllegalArgumentException("invalid runtimeSpeedMultiplier");
        if ((seasonId == null) != (seasonDisplayName == null)) throw new IllegalArgumentException("season fields must both be null or non-null");
        nations = copy(nations); strategicPoints = copy(strategicPoints); strategicEdges = copy(strategicEdges);
        armies = copy(armies); armyOrders = copy(armyOrders); armyOperationQueues = copy(armyOperationQueues);
        personalWallets = copy(personalWallets); strategicResourceStockpiles = copy(strategicResourceStockpiles);
        localResourceStockpiles = copy(localResourceStockpiles); generals = copy(generals);
        facilities = copy(facilities); facilityConstructions = copy(facilityConstructions); researchStates = copy(researchStates);
    }

    private static <T> List<T> copy(List<T> value) { return value == null ? List.of() : List.copyOf(value); }

    public GameSnapshot(int schemaVersion, long createdAtEpochMillis, long runtimeElapsedMillis,
                        boolean runtimePaused, double runtimeSpeedMultiplier, String seasonId, String seasonDisplayName,
                        List<NationSnapshot> nations, List<StrategicPointSnapshot> strategicPoints,
                        List<StrategicEdgeSnapshot> strategicEdges, List<ArmySnapshot> armies) {
        this(schemaVersion, createdAtEpochMillis, runtimeElapsedMillis, runtimePaused, runtimeSpeedMultiplier,
                seasonId, seasonDisplayName, nations, strategicPoints, strategicEdges, armies,
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    }

    public GameSnapshot(int schemaVersion, long createdAtEpochMillis, long runtimeElapsedMillis,
                        boolean runtimePaused, double runtimeSpeedMultiplier, String seasonId, String seasonDisplayName,
                        List<NationSnapshot> nations, List<StrategicPointSnapshot> strategicPoints,
                        List<StrategicEdgeSnapshot> strategicEdges, List<ArmySnapshot> armies,
                        List<ArmyOrderSnapshot> armyOrders, List<ArmyOperationQueueSnapshot> armyOperationQueues) {
        this(schemaVersion, createdAtEpochMillis, runtimeElapsedMillis, runtimePaused, runtimeSpeedMultiplier,
                seasonId, seasonDisplayName, nations, strategicPoints, strategicEdges, armies, armyOrders,
                armyOperationQueues, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    }

    public GameSnapshot(int schemaVersion, long createdAtEpochMillis, long runtimeElapsedMillis,
                        boolean runtimePaused, double runtimeSpeedMultiplier, String seasonId, String seasonDisplayName,
                        List<NationSnapshot> nations, List<StrategicPointSnapshot> strategicPoints,
                        List<StrategicEdgeSnapshot> strategicEdges, List<ArmySnapshot> armies,
                        List<ArmyOrderSnapshot> armyOrders, List<ArmyOperationQueueSnapshot> armyOperationQueues,
                        List<PersonalWalletSnapshot> personalWallets) {
        this(schemaVersion, createdAtEpochMillis, runtimeElapsedMillis, runtimePaused, runtimeSpeedMultiplier,
                seasonId, seasonDisplayName, nations, strategicPoints, strategicEdges, armies, armyOrders,
                armyOperationQueues, personalWallets, List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    }

    public GameSnapshot(int schemaVersion, long createdAtEpochMillis, long runtimeElapsedMillis,
                        boolean runtimePaused, double runtimeSpeedMultiplier, String seasonId, String seasonDisplayName,
                        List<NationSnapshot> nations, List<StrategicPointSnapshot> strategicPoints,
                        List<StrategicEdgeSnapshot> strategicEdges, List<ArmySnapshot> armies,
                        List<ArmyOrderSnapshot> armyOrders, List<ArmyOperationQueueSnapshot> armyOperationQueues,
                        List<PersonalWalletSnapshot> personalWallets,
                        List<StrategicResourceStockpileSnapshot> strategicResourceStockpiles) {
        this(schemaVersion, createdAtEpochMillis, runtimeElapsedMillis, runtimePaused, runtimeSpeedMultiplier,
                seasonId, seasonDisplayName, nations, strategicPoints, strategicEdges, armies, armyOrders,
                armyOperationQueues, personalWallets, strategicResourceStockpiles, List.of(), List.of(), List.of(), List.of(), List.of());
    }

    /** Source-compatible v12-shape constructor used by existing snapshot code/tests. */
    public GameSnapshot(int schemaVersion, long createdAtEpochMillis, long runtimeElapsedMillis,
                        boolean runtimePaused, double runtimeSpeedMultiplier, String seasonId, String seasonDisplayName,
                        List<NationSnapshot> nations, List<StrategicPointSnapshot> strategicPoints,
                        List<StrategicEdgeSnapshot> strategicEdges, List<ArmySnapshot> armies,
                        List<ArmyOrderSnapshot> armyOrders, List<ArmyOperationQueueSnapshot> armyOperationQueues,
                        List<PersonalWalletSnapshot> personalWallets,
                        List<StrategicResourceStockpileSnapshot> strategicResourceStockpiles,
                        List<LocalResourceStockpileSnapshot> localResourceStockpiles, List<GeneralSnapshot> generals) {
        this(schemaVersion, createdAtEpochMillis, runtimeElapsedMillis, runtimePaused, runtimeSpeedMultiplier,
                seasonId, seasonDisplayName, nations, strategicPoints, strategicEdges, armies, armyOrders,
                armyOperationQueues, personalWallets, strategicResourceStockpiles, localResourceStockpiles, generals,
                List.of(), List.of(), List.of());
    }

    /** Source-compatible v13-shape constructor. */
    public GameSnapshot(int schemaVersion, long createdAtEpochMillis, long runtimeElapsedMillis,
                        boolean runtimePaused, double runtimeSpeedMultiplier, String seasonId, String seasonDisplayName,
                        List<NationSnapshot> nations, List<StrategicPointSnapshot> strategicPoints,
                        List<StrategicEdgeSnapshot> strategicEdges, List<ArmySnapshot> armies,
                        List<ArmyOrderSnapshot> armyOrders, List<ArmyOperationQueueSnapshot> armyOperationQueues,
                        List<PersonalWalletSnapshot> personalWallets,
                        List<StrategicResourceStockpileSnapshot> strategicResourceStockpiles,
                        List<LocalResourceStockpileSnapshot> localResourceStockpiles, List<GeneralSnapshot> generals,
                        List<FacilitySnapshot> facilities, List<FacilityConstructionSnapshot> facilityConstructions) {
        this(schemaVersion, createdAtEpochMillis, runtimeElapsedMillis, runtimePaused, runtimeSpeedMultiplier,
                seasonId, seasonDisplayName, nations, strategicPoints, strategicEdges, armies, armyOrders,
                armyOperationQueues, personalWallets, strategicResourceStockpiles, localResourceStockpiles, generals,
                facilities, facilityConstructions, List.of());
    }
}
