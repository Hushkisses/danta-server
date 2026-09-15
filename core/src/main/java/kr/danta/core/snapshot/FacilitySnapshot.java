package kr.danta.core.snapshot;

import kr.danta.core.facility.FacilityTier;

/** Installed facility snapshot entry (DEV-081 / schema v13). */
public record FacilitySnapshot(String pointId, String facilityId, FacilityTier tier) {}
