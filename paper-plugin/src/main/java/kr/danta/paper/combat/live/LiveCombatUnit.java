package kr.danta.paper.combat.live;

import kr.danta.core.combat.TroopType;

import java.util.Objects;
import java.util.UUID;

/** DEV-115 immutable identity for one tracked live battlefield unit. */
public record LiveCombatUnit(
        UUID unitId,
        CombatSide side,
        TroopType troopType,
        UUID primaryEntityId,
        UUID mountEntityId,
        String visualProfileId
) {
    public LiveCombatUnit {
        Objects.requireNonNull(unitId, "unitId");
        Objects.requireNonNull(side, "side");
        Objects.requireNonNull(troopType, "troopType");
        Objects.requireNonNull(primaryEntityId, "primaryEntityId");
        Objects.requireNonNull(visualProfileId, "visualProfileId");
        if (visualProfileId.isBlank()) throw new IllegalArgumentException("visualProfileId is blank");
    }

    public boolean mounted() {
        return mountEntityId != null;
    }
}
