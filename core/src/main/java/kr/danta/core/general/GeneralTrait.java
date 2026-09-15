package kr.danta.core.general;

import java.util.Objects;

/**
 * DEV-072A trait identity.
 * Concrete trait catalog/effects are data/balance work and are not invented here.
 */
public record GeneralTrait(String traitId) {
    public GeneralTrait {
        traitId = requireId(traitId, "traitId");
    }

    private static String requireId(String value, String label) {
        Objects.requireNonNull(value, label);
        String normalized = value.trim();
        if (!normalized.matches("[A-Za-z0-9_-]{1,48}"))
            throw new IllegalArgumentException(label + " must match [A-Za-z0-9_-]{1,48}");
        return normalized;
    }
}
