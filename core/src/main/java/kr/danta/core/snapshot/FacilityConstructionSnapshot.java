package kr.danta.core.snapshot;

import kr.danta.core.facility.FacilityTier;

import java.util.UUID;

/** Pending runtime construction snapshot entry (DEV-081 / schema v13). */
public record FacilityConstructionSnapshot(
        UUID constructionId, String pointId, String facilityId, FacilityTier targetTier, long dueRuntimeMillis
) {}
