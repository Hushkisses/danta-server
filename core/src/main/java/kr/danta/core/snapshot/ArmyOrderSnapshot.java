package kr.danta.core.snapshot;

import kr.danta.core.army.ArmyOrderStatus;
import kr.danta.core.army.ArmyOrderType;

/** Persisted DEV-033 active army movement order and runtime deadline. */
public record ArmyOrderSnapshot(
        String orderId,
        String armyId,
        ArmyOrderType type,
        String originPointId,
        String destinationPointId,
        String edgeId,
        ArmyOrderStatus status,
        long dueRuntimeMillis
) {}
