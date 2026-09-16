package kr.danta.paper.combat.live;

/** Replaceable development renderer metadata for a live combat unit. */
public record CombatUnitVisualProfile(
        String profileId,
        String bodyEntityKey,
        String mountEntityKey,
        String displayName,
        String mainHandMaterialKey
) {
    public CombatUnitVisualProfile {
        if (profileId == null || profileId.isBlank()) {
            throw new IllegalArgumentException("profileId is blank");
        }
        if (bodyEntityKey == null || bodyEntityKey.isBlank()) {
            throw new IllegalArgumentException("bodyEntityKey is blank");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("displayName is blank");
        }
        if (mainHandMaterialKey == null || mainHandMaterialKey.isBlank()) {
            throw new IllegalArgumentException("mainHandMaterialKey is blank");
        }
    }
}
