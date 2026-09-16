package kr.danta.core.siege;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * DEV-111 wall-clock reservation service.
 * Uses java.time Clock, deliberately independent from RuntimeClockService.
 * Exact gameplay lead/confirmation windows remain policy/configuration values.
 */
public final class SiegeReservationService {
    private final SiegeService sieges;
    private final Clock clock;
    private final Policy policy;
    private final Map<String, SiegeReservation> reservations = new LinkedHashMap<>();

    public SiegeReservationService(SiegeService sieges, Clock clock, Policy policy) {
        this.sieges = Objects.requireNonNull(sieges);
        this.clock = Objects.requireNonNull(clock);
        this.policy = Objects.requireNonNull(policy);
    }

    public synchronized SiegeReservation reserve(String siegeId, Instant scheduledAt) {
        SiegeInstance siege = sieges.find(siegeId).orElseThrow(() -> new IllegalArgumentException("siege not found: " + siegeId));
        if (siege.phase() != SiegePhase.CREATED) throw new IllegalStateException("siege must be CREATED");
        Instant now = clock.instant();
        if (scheduledAt.isBefore(now.plus(policy.minimumLeadTime())))
            throw new IllegalStateException("scheduled time is too soon");
        SiegeReservation reservation = new SiegeReservation(siegeId, scheduledAt, null);
        reservations.put(siegeId, reservation);
        siege.schedule();
        return reservation;
    }

    public synchronized SiegeReservation confirm(String siegeId) {
        SiegeReservation current = require(siegeId);
        if (current.confirmed()) return current;
        Instant now = clock.instant();
        if (now.isAfter(current.scheduledAt().minus(policy.confirmationDeadlineBeforeStart())))
            throw new IllegalStateException("confirmation deadline has passed");
        SiegeReservation confirmed = new SiegeReservation(siegeId, current.scheduledAt(), now);
        reservations.put(siegeId, confirmed);
        return confirmed;
    }

    public synchronized SiegeInstance activateDue(String siegeId) {
        SiegeReservation reservation = require(siegeId);
        if (!reservation.confirmed()) throw new IllegalStateException("siege reservation is not confirmed");
        if (clock.instant().isBefore(reservation.scheduledAt())) throw new IllegalStateException("scheduled time has not arrived");
        SiegeInstance siege = sieges.find(siegeId).orElseThrow();
        siege.activate();
        return siege;
    }

    public synchronized Optional<SiegeReservation> find(String siegeId) { return Optional.ofNullable(reservations.get(siegeId)); }

    private SiegeReservation require(String id) {
        SiegeReservation r = reservations.get(id);
        if (r == null) throw new IllegalArgumentException("reservation not found: " + id);
        return r;
    }

    public interface Policy {
        Duration minimumLeadTime();
        Duration confirmationDeadlineBeforeStart();
    }
}
