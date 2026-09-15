package kr.danta.core.combat;

import java.util.Objects;

/**
 * DEV-062 post-combat result. Casualty amounts still come from the provisional
 * DEV-042 loss policy; DEV-062 adds explicit morale/rout/pursuit semantics.
 */
public record CombatResolutionV1(
        CombatV1Result combat,
        CombatResolution losses,
        RetreatPursuitResult first,
        RetreatPursuitResult second
) {
    public CombatResolutionV1 {
        Objects.requireNonNull(combat, "combat");
        Objects.requireNonNull(losses, "losses");
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
    }
}
