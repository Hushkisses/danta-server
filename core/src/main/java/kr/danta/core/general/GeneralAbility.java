package kr.danta.core.general;

import java.util.Objects;

/**
 * DEV-072A ability identity.
 * Activation rules, cooldowns and numeric effects remain unresolved design data.
 */
public record GeneralAbility(String abilityId) {
    public GeneralAbility {
        abilityId = requireId(abilityId, "abilityId");
    }

    private static String requireId(String value, String label) {
        Objects.requireNonNull(value, label);
        String normalized = value.trim();
        if (!normalized.matches("[A-Za-z0-9_-]{1,48}"))
            throw new IllegalArgumentException(label + " must match [A-Za-z0-9_-]{1,48}");
        return normalized;
    }
}
