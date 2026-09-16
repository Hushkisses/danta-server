package kr.danta.paper.combat.live;

import java.util.UUID;

/** Tracks the rider/mount identity that together form one logical cavalry combat unit. */
public record CavalryCompositeState(UUID riderEntityId, UUID mountEntityId) {
    public CavalryCompositeState {
        if (riderEntityId == null) throw new NullPointerException("riderEntityId");
        if (mountEntityId == null) throw new NullPointerException("mountEntityId");
    }

    public boolean shouldRetire(boolean riderAlive, boolean mountAlive) {
        return !riderAlive || !mountAlive;
    }
}
