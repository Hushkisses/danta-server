package kr.danta.paper.combat.live;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Retires a cavalry logical unit when either rider or mount is gone. */
public final class CavalryRetirementPolicy {

    public Optional<RetirementRequest> evaluate(
            CavalryCompositeState state,
            boolean riderAlive,
            boolean mountAlive
    ) {
        if (state == null) throw new NullPointerException("state");
        if (!state.shouldRetire(riderAlive, mountAlive)) return Optional.empty();
        return Optional.of(new RetirementRequest(List.of(state.riderEntityId(), state.mountEntityId())));
    }

    public record RetirementRequest(List<UUID> entityIds) {
        public RetirementRequest {
            if (entityIds == null) throw new NullPointerException("entityIds");
            entityIds = List.copyOf(entityIds);
        }
    }
}
