package kr.danta.core.snapshot;

import java.util.List;

public record SiegeRuntimeSnapshot(
        List<SiegeInstanceSnapshot> instances,
        List<SiegeReservationSnapshot> reservations,
        List<SiegeProgressSnapshot> progress,
        List<SiegeParticipantSnapshot> participants,
        List<SiegeMoraleSnapshot> morale
) {
    public SiegeRuntimeSnapshot {
        instances = instances == null ? List.of() : List.copyOf(instances);
        reservations = reservations == null ? List.of() : List.copyOf(reservations);
        progress = progress == null ? List.of() : List.copyOf(progress);
        participants = participants == null ? List.of() : List.copyOf(participants);
        morale = morale == null ? List.of() : List.copyOf(morale);
    }

    public static SiegeRuntimeSnapshot empty() {
        return new SiegeRuntimeSnapshot(List.of(), List.of(), List.of(), List.of(), List.of());
    }

    public boolean isEmpty() {
        return instances.isEmpty() && reservations.isEmpty() && progress.isEmpty()
                && participants.isEmpty() && morale.isEmpty();
    }
}
