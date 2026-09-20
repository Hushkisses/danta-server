package kr.danta.core.snapshot;

import kr.danta.core.siege.SiegeEngagementProfile;
import kr.danta.core.siege.SiegeStage;

public record SiegeProgressSnapshot(
        String pointId,
        SiegeEngagementProfile profile,
        SiegeStage stage,
        boolean resumeRequired
) {}
