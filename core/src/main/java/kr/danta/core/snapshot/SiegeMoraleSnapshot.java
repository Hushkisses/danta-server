package kr.danta.core.snapshot;

import kr.danta.core.siege.SiegeSide;

public record SiegeMoraleSnapshot(
        String pointId,
        SiegeSide side,
        int moraleDelta
) {}
