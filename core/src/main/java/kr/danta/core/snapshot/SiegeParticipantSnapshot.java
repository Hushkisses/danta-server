package kr.danta.core.snapshot;

import kr.danta.core.siege.SiegeSide;

import java.util.UUID;

public record SiegeParticipantSnapshot(
        String pointId,
        UUID playerId,
        SiegeSide side,
        boolean eliminated,
        String previousGameMode
) {}
