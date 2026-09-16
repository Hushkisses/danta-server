package kr.danta.paper.combat.live;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Minimal DEV-115 lifecycle and per-unit cooldown state for the live combat demo. */
public final class LiveCombatRuntimeState {
    private final Map<UUID, Long> nextAttackAtMillis = new HashMap<>();
    private boolean active;

    public boolean begin() {
        if (active) return false;
        active = true;
        return true;
    }

    public void finish() {
        active = false;
        nextAttackAtMillis.clear();
    }

    public boolean active() {
        return active;
    }

    public boolean tryAcquireAttack(UUID unitId, long nowMillis, long cooldownMillis) {
        if (unitId == null) throw new NullPointerException("unitId");
        if (cooldownMillis < 0L) throw new IllegalArgumentException("cooldownMillis must be >= 0");

        long nextAllowed = nextAttackAtMillis.getOrDefault(unitId, Long.MIN_VALUE);
        if (nowMillis < nextAllowed) return false;

        long next;
        try {
            next = Math.addExact(nowMillis, cooldownMillis);
        } catch (ArithmeticException ex) {
            next = Long.MAX_VALUE;
        }
        nextAttackAtMillis.put(unitId, next);
        return true;
    }
}
