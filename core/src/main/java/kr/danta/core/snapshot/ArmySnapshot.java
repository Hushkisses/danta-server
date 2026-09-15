package kr.danta.core.snapshot;

import kr.danta.core.army.ArmyStatus;
import kr.danta.core.army.ExpeditionSupplyLevel;

/** Persisted army state. DEV-056 adds expedition supply fields. */
public record ArmySnapshot(
        String armyId,
        String ownerNationId,
        String locationPointId,
        ArmyStatus status,
        long baseTroops,
        ExpeditionSupplyLevel expeditionSupplyLevel,
        long carriedFood
) {
    public ArmySnapshot(String armyId, String ownerNationId, String locationPointId, ArmyStatus status, long baseTroops) {
        this(armyId, ownerNationId, locationPointId, status, baseTroops, null, 0L);
    }
}
