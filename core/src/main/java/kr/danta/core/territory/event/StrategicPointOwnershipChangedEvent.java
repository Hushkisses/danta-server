package kr.danta.core.territory.event;

import kr.danta.core.event.DomainEvent;

import java.util.Optional;

/** Emitted after the authoritative owner of a strategic point changes. */
public record StrategicPointOwnershipChangedEvent(
        String pointId,
        String previousOwnerNationId,
        String newOwnerNationId,
        String reason
) implements DomainEvent {
    public Optional<String> previousOwner() { return Optional.ofNullable(previousOwnerNationId); }
    public Optional<String> newOwner() { return Optional.ofNullable(newOwnerNationId); }
}
