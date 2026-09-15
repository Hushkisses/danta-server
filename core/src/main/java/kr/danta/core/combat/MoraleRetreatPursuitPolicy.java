package kr.danta.core.combat;

import java.util.Objects;

/**
 * DEV-062 minimum morale/retreat/pursuit policy.
 *
 * Design v0.3 fixes the five morale stages, says most defeats become routs rather
 * than annihilation, and gives cavalry value in pursuit. It does not fix numeric
 * morale multipliers or pursuit casualty rates, so this ticket exposes those
 * semantics without inventing final balance numbers.
 */
public final class MoraleRetreatPursuitPolicy {
    private final CombatLossPolicy lossPolicy;

    public MoraleRetreatPursuitPolicy() {
        this(new CombatLossPolicy());
    }

    MoraleRetreatPursuitPolicy(CombatLossPolicy lossPolicy) {
        this.lossPolicy = Objects.requireNonNull(lossPolicy, "lossPolicy");
    }

    public CombatResolutionV1 apply(CombatV1Result combat, RetreatPursuitContext context) {
        Objects.requireNonNull(combat, "combat");
        Objects.requireNonNull(context, "context");

        CombatResolution losses = lossPolicy.apply(combat.finalResult());
        boolean firstRouted = losses.first().retreatRequired();
        boolean secondRouted = losses.second().retreatRequired();

        boolean firstPursuit = secondRouted && combat.finalResult().first().troopType() == TroopType.CAVALRY;
        boolean secondPursuit = firstRouted && combat.finalResult().second().troopType() == TroopType.CAVALRY;

        return new CombatResolutionV1(
                combat,
                losses,
                new RetreatPursuitResult(losses.first().sideId(), context.firstMorale(), firstRouted, firstPursuit),
                new RetreatPursuitResult(losses.second().sideId(), context.secondMorale(), secondRouted, secondPursuit)
        );
    }
}
