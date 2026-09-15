package kr.danta.core.general;

import java.util.Map;
import java.util.Optional;

/**
 * DEV-076E maps the provisional elite roster's ability IDs to semantic hooks.
 * Balance coefficients belong to a later configurable policy, not this catalog.
 */
public final class GeneralAbilityCatalog {
    private static final Map<String, GeneralAbilityEffect> EFFECTS = Map.ofEntries(
            effect("iron_formation", GeneralAbilityHook.COMBAT_INFANTRY_DEFENSE),
            effect("relentless_pursuit", GeneralAbilityHook.COMBAT_PURSUIT),
            effect("volley_fire", GeneralAbilityHook.COMBAT_ARCHER_FIREPOWER),
            effect("prosperous_domain", GeneralAbilityHook.POINT_GOLD_REVENUE),
            effect("production_management", GeneralAbilityHook.POINT_STRATEGIC_RESOURCE_PRODUCTION),
            effect("supply_mastery", GeneralAbilityHook.ARMY_SUPPLY_EFFICIENCY),
            effect("fortress_command", GeneralAbilityHook.POINT_DEFENSE),
            effect("supreme_command", GeneralAbilityHook.COMBAT_COMBINED_ARMS),
            effect("grand_reform", GeneralAbilityHook.NATION_ADMINISTRATIVE_CAPACITY),
            effect("deep_campaign", GeneralAbilityHook.ARMY_EXPEDITION)
    );

    private GeneralAbilityCatalog() {}

    public static Optional<GeneralAbilityEffect> find(String abilityId) {
        return Optional.ofNullable(EFFECTS.get(abilityId));
    }

    private static Map.Entry<String, GeneralAbilityEffect> effect(String id, GeneralAbilityHook hook) {
        return Map.entry(id, new GeneralAbilityEffect(id, hook));
    }
}
