package kr.danta.core.general;

import java.util.Objects;

/**
 * DEV-076E semantic hook for a special ability.
 * Numeric coefficients are intentionally not stored here.
 */
public record GeneralAbilityEffect(String abilityId, GeneralAbilityHook hook) {
    public GeneralAbilityEffect {
        abilityId = Objects.requireNonNull(abilityId, "abilityId").trim();
        Objects.requireNonNull(hook, "hook");
        if (abilityId.isEmpty()) throw new IllegalArgumentException("abilityId must not be blank");
    }
}
