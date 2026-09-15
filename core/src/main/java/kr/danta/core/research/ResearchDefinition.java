package kr.danta.core.research;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Immutable data-driven research definition. Exact balance values live in content data, not code. */
public record ResearchDefinition(
        String researchId,
        String displayName,
        ResearchField field,
        ResearchTier tier,
        long durationRuntimeMillis,
        long goldCost,
        Map<String, Long> resourceCosts,
        List<String> prerequisites,
        String doctrineKey,
        String requiredMajorPointType
) {
    public ResearchDefinition {
        researchId = requireId(researchId, "researchId");
        displayName = Objects.requireNonNull(displayName, "displayName").trim();
        if (displayName.isEmpty()) throw new IllegalArgumentException("displayName is blank");
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(tier, "tier");
        if (durationRuntimeMillis < 0) throw new IllegalArgumentException("durationRuntimeMillis < 0");
        if (goldCost < 0) throw new IllegalArgumentException("goldCost < 0");
        resourceCosts = Map.copyOf(Objects.requireNonNull(resourceCosts, "resourceCosts"));
        prerequisites = List.copyOf(Objects.requireNonNull(prerequisites, "prerequisites"));
        doctrineKey = normalizeOptional(doctrineKey);
        requiredMajorPointType = normalizeOptional(requiredMajorPointType);
        resourceCosts.forEach((key, value) -> { requireId(key, "resource key"); if (value < 0) throw new IllegalArgumentException("negative resource cost: " + key); });
        prerequisites.forEach(id -> requireId(id, "prerequisite"));
        if (tier == ResearchTier.TIER_4 && doctrineKey == null) throw new IllegalArgumentException("TIER_4 requires doctrineKey: " + researchId);
    }
    private static String requireId(String value, String label) {
        Objects.requireNonNull(value, label);
        String normalized = value.trim().toLowerCase();
        if (!normalized.matches("[a-z0-9_.-]+")) throw new IllegalArgumentException("invalid " + label + ": " + value);
        return normalized;
    }
    private static String normalizeOptional(String value) { return value == null || value.isBlank() ? null : value.trim().toLowerCase(); }
}
