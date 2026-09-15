package kr.danta.core.combat;

public record BattlefieldSideResult(
        String sideId,
        int troopCount,
        int battlefieldCapacity,
        int committedTroops,
        int reserveTroops,
        double utilization
) {
    public BattlefieldSideResult {
        if (sideId == null || sideId.isBlank()) throw new IllegalArgumentException("sideId must not be blank");
        if (troopCount <= 0 || battlefieldCapacity <= 0) throw new IllegalArgumentException("counts must be positive");
        if (committedTroops <= 0 || committedTroops > troopCount) throw new IllegalArgumentException("invalid committedTroops");
        if (reserveTroops != troopCount - committedTroops) throw new IllegalArgumentException("reserveTroops mismatch");
        if (!Double.isFinite(utilization) || utilization <= 0.0 || utilization > 1.0)
            throw new IllegalArgumentException("utilization must be in (0, 1]");
    }
}
