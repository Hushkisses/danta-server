package kr.danta.core.snapshot;

public record SiegeReservationSnapshot(
        String siegeId,
        long scheduledAtEpochMillis,
        Long confirmedAtEpochMillis
) {}
