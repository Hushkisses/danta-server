package kr.danta.core.combat;

public record CombatSideResult(
        String sideId,
        TroopType troopType,
        int initialTroops,
        double basePower,
        double counterMultiplier,
        double effectivePower
) {}
