package kr.danta.core.general;

import java.util.List;
import java.util.Objects;

/**
 * DEV-076A data-driven general definition.
 * ownerNationId is intentionally absent: initial ownership/acquisition is a separate rule.
 */
public record GeneralDefinition(
        String id,
        String displayName,
        GeneralGrade grade,
        int level,
        GeneralStats stats,
        List<String> traitIds,
        List<String> abilityIds
) {
    public GeneralDefinition {
        id = requireId(id, "id");
        displayName = Objects.requireNonNull(displayName, "displayName").trim();
        if (displayName.isEmpty()) throw new IllegalArgumentException("displayName must not be blank");
        Objects.requireNonNull(grade, "grade");
        if (level < GeneralState.MIN_LEVEL || level > GeneralState.MAX_LEVEL)
            throw new IllegalArgumentException("level must be between 1 and 10");
        Objects.requireNonNull(stats, "stats");
        traitIds = validatedIds(traitIds, "traitIds");
        abilityIds = validatedIds(abilityIds, "abilityIds");
        if (traitIds.size() > 2)
            throw new IllegalArgumentException("a general may have at most 2 base traits");
        if (abilityIds.size() > 1)
            throw new IllegalArgumentException("a general may have at most 1 unique ability");
    }

    private static List<String> validatedIds(List<String> values, String label) {
        Objects.requireNonNull(values, label);
        List<String> copy = values.stream().map(v -> requireId(v, label)).toList();
        if (copy.stream().distinct().count() != copy.size())
            throw new IllegalArgumentException(label + " must not contain duplicates");
        return copy;
    }

    private static String requireId(String value, String label) {
        Objects.requireNonNull(value, label);
        String v = value.trim();
        if (!v.matches("[A-Za-z0-9_-]{1,48}"))
            throw new IllegalArgumentException(label + " must match [A-Za-z0-9_-]{1,48}");
        return v;
    }
}
