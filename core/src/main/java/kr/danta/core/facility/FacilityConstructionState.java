package kr.danta.core.facility;

import java.util.Objects;
import java.util.UUID;

/** Authoritative pending facility construction/upgrade state for DEV-081. */
public record FacilityConstructionState(
        UUID constructionId,
        String pointId,
        String facilityId,
        FacilityTier targetTier,
        long dueRuntimeMillis
) {
    public FacilityConstructionState {
        Objects.requireNonNull(constructionId, "constructionId");
        pointId = requireId(pointId, "pointId");
        facilityId = requireFacilityId(facilityId);
        Objects.requireNonNull(targetTier, "targetTier");
        if (dueRuntimeMillis < 0L) throw new IllegalArgumentException("dueRuntimeMillis must be >= 0");
    }

    private static String requireId(String value, String label) {
        Objects.requireNonNull(value, label);
        String normalized = value.trim();
        if (!normalized.matches("[A-Za-z0-9_-]{1,48}")) throw new IllegalArgumentException("invalid " + label);
        return normalized;
    }

    private static String requireFacilityId(String value) {
        Objects.requireNonNull(value, "facilityId");
        String normalized = value.trim();
        if (!normalized.matches("[A-Za-z0-9_.-]{1,48}")) throw new IllegalArgumentException("invalid facilityId");
        return normalized;
    }
}
