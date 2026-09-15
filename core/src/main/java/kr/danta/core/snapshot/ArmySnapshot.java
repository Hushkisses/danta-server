package kr.danta.core.snapshot;

import kr.danta.core.army.ArmyStatus;

/** Persisted DEV-030 army state. */
public record ArmySnapshot(
        String armyId,
        String ownerNationId,
        String locationPointId,
        ArmyStatus status,
        long baseTroops
) {}
