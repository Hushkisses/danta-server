package kr.danta.core.combat;

import java.util.Objects;

public record CombatSideInput(
        String sideId,
        TroopType troopType,
        int troopCount,
        double commanderMultiplier,
        double researchMultiplier,
        double terrainMultiplier,
        double supplyMultiplier,
        double moraleMultiplier
) {
    public CombatSideInput {
        Objects.requireNonNull(sideId, "sideId");
        Objects.requireNonNull(troopType, "troopType");
        if (sideId.isBlank()) throw new IllegalArgumentException("sideId must not be blank");
        if (troopCount <= 0) throw new IllegalArgumentException("troopCount must be positive");
        validate(commanderMultiplier, "commanderMultiplier");
        validate(researchMultiplier, "researchMultiplier");
        validate(terrainMultiplier, "terrainMultiplier");
        validate(supplyMultiplier, "supplyMultiplier");
        validate(moraleMultiplier, "moraleMultiplier");
    }

    public static CombatSideInput neutral(String sideId, TroopType troopType, int troopCount) {
        return new CombatSideInput(sideId, troopType, troopCount, 1.0, 1.0, 1.0, 1.0, 1.0);
    }

    private static void validate(double value, String label) {
        if (!Double.isFinite(value) || value <= 0.0) throw new IllegalArgumentException(label + " must be positive");
    }
}
