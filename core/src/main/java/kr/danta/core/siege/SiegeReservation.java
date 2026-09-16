package kr.danta.core.siege;

import java.time.Instant;
import java.util.Objects;

/** DEV-111 real-world-time reservation attached to a scheduled siege. */
public record SiegeReservation(String siegeId, Instant scheduledAt, Instant confirmedAt) {
    public SiegeReservation {
        siegeId = require(siegeId, "siegeId");
        scheduledAt = Objects.requireNonNull(scheduledAt, "scheduledAt");
    }
    public boolean confirmed() { return confirmedAt != null; }
    private static String require(String v, String n) {
        Objects.requireNonNull(v, n);
        if (v.isBlank()) throw new IllegalArgumentException(n + " must not be blank");
        return v;
    }
}
