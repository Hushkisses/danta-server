package kr.danta.paper.combat.live;

import java.util.EnumMap;
import java.util.Map;

import kr.danta.core.combat.TroopType;

/** Development-only renderer catalog; troop identity remains independent from Bukkit mob classes. */
public final class CombatUnitVisualCatalog {
    private final Map<TroopType, CombatUnitVisualProfile> profiles;

    public CombatUnitVisualCatalog(Map<TroopType, CombatUnitVisualProfile> profiles) {
        this.profiles = Map.copyOf(profiles);
    }

    public CombatUnitVisualProfile profileFor(TroopType troopType) {
        if (troopType == null) throw new NullPointerException("troopType");
        CombatUnitVisualProfile profile = profiles.get(troopType);
        if (profile == null) throw new IllegalArgumentException("missing visual profile: " + troopType);
        return profile;
    }

    public static CombatUnitVisualCatalog developmentDefaults() {
        EnumMap<TroopType, CombatUnitVisualProfile> profiles = new EnumMap<>(TroopType.class);
        profiles.put(TroopType.INFANTRY,
                new CombatUnitVisualProfile("dev_infantry", "HUSK", null, "보병", "IRON_SWORD"));
        profiles.put(TroopType.SPEARMEN,
                new CombatUnitVisualProfile("dev_spearmen", "HUSK", null, "창병", "TRIDENT"));
        profiles.put(TroopType.ARCHERS,
                new CombatUnitVisualProfile("dev_archers", "SKELETON", null, "궁병", "BOW"));
        profiles.put(TroopType.CAVALRY,
                new CombatUnitVisualProfile("dev_cavalry", "HUSK", "HORSE", "기병", "IRON_SWORD"));
        profiles.put(TroopType.MAGIC,
                new CombatUnitVisualProfile("dev_magic", "HUSK", null, "마법병", "BLAZE_ROD"));
        return new CombatUnitVisualCatalog(profiles);
    }
}
