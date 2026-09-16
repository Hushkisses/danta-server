package kr.danta.core.siege;

import java.util.Objects;

/** DEV-110 one authoritative siege state machine instance. */
public final class SiegeInstance {
    private final String siegeId;
    private final String pointId;
    private final String attackerNationId;
    private final String defenderNationId;
    private SiegePhase phase;
    private String result;

    public SiegeInstance(String siegeId, String pointId, String attackerNationId, String defenderNationId) {
        this.siegeId = require(siegeId, "siegeId");
        this.pointId = require(pointId, "pointId");
        this.attackerNationId = require(attackerNationId, "attackerNationId");
        this.defenderNationId = require(defenderNationId, "defenderNationId");
        if (attackerNationId.equals(defenderNationId)) throw new IllegalArgumentException("attacker and defender must differ");
        this.phase = SiegePhase.CREATED;
    }

    public synchronized void schedule() { transition(SiegePhase.CREATED, SiegePhase.SCHEDULED); }
    public synchronized void activate() { transition(SiegePhase.SCHEDULED, SiegePhase.ACTIVE); }
    public synchronized void resolve(String result) {
        transition(SiegePhase.ACTIVE, SiegePhase.RESOLVED);
        this.result = require(result, "result");
    }
    public synchronized void cancel() {
        if (phase != SiegePhase.CREATED && phase != SiegePhase.SCHEDULED)
            throw new IllegalStateException("only created/scheduled siege can be cancelled");
        phase = SiegePhase.CANCELLED;
    }

    private void transition(SiegePhase expected, SiegePhase next) {
        if (phase != expected) throw new IllegalStateException("siege phase must be " + expected + " but was " + phase);
        phase = next;
    }
    private static String require(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) throw new IllegalArgumentException(name + " must not be blank");
        return value;
    }
    public String siegeId() { return siegeId; }
    public String pointId() { return pointId; }
    public String attackerNationId() { return attackerNationId; }
    public String defenderNationId() { return defenderNationId; }
    public synchronized SiegePhase phase() { return phase; }
    public synchronized String result() { return result; }
}
