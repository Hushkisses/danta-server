package kr.danta.core.general;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Provisional/configurable coefficients for general ability hooks.
 * Defaults are neutral so unresolved balance never changes gameplay silently.
 */
public final class GeneralAbilityBalance {
    private final Map<GeneralAbilityHook, Double> multipliers;
    private final Map<GeneralAbilityHook, Double> additiveBonuses;

    public GeneralAbilityBalance(Map<GeneralAbilityHook, Double> multipliers,
                                 Map<GeneralAbilityHook, Double> additiveBonuses) {
        this.multipliers = copy(multipliers);
        this.additiveBonuses = copy(additiveBonuses);
    }

    public static GeneralAbilityBalance neutral() {
        return new GeneralAbilityBalance(Map.of(), Map.of());
    }

    public double multiplier(GeneralAbilityHook hook) {
        return multipliers.getOrDefault(Objects.requireNonNull(hook), 1.0);
    }

    public double additiveBonus(GeneralAbilityHook hook) {
        return additiveBonuses.getOrDefault(Objects.requireNonNull(hook), 0.0);
    }

    private static Map<GeneralAbilityHook, Double> copy(Map<GeneralAbilityHook, Double> source) {
        EnumMap<GeneralAbilityHook, Double> result = new EnumMap<>(GeneralAbilityHook.class);
        if (source != null) result.putAll(source);
        return Map.copyOf(result);
    }
}
