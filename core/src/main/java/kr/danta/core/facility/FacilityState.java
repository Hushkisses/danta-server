package kr.danta.core.facility;

import java.util.Objects;

/** One occupied facility slot at a strategic point. */
public record FacilityState(String facilityId, FacilityTier tier) {
    public FacilityState {
        Objects.requireNonNull(facilityId, "facilityId");
        Objects.requireNonNull(tier, "tier");
        facilityId = facilityId.trim();
        if (!facilityId.matches("[A-Za-z0-9_.-]{1,48}")) throw new IllegalArgumentException("invalid facilityId");
    }
}
