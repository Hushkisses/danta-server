package kr.danta.core.combat;

import java.util.Objects;

public record TroopTypeDefinition(
        TroopType type,
        TroopRole role,
        double basePower,
        TroopType strongAgainst,
        double strongAgainstMultiplier
) {
    public TroopTypeDefinition {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(strongAgainst, "strongAgainst");
        if (!Double.isFinite(basePower) || basePower <= 0.0) throw new IllegalArgumentException("basePower must be positive");
        if (!Double.isFinite(strongAgainstMultiplier) || strongAgainstMultiplier < 1.0)
            throw new IllegalArgumentException("strongAgainstMultiplier must be at least 1.0");
    }
}
