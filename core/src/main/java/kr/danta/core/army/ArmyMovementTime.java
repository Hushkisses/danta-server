package kr.danta.core.army;

import kr.danta.core.territory.BattlefieldTag;

import java.time.Duration;
import java.util.Objects;
import java.util.Set;

/** DEV-032 immutable movement-duration calculation result. */
public record ArmyMovementTime(
        Duration baseDuration,
        Duration effectiveDuration,
        double multiplier,
        Set<BattlefieldTag> routeTags
) {
    public ArmyMovementTime {
        baseDuration = Objects.requireNonNull(baseDuration, "baseDuration");
        effectiveDuration = Objects.requireNonNull(effectiveDuration, "effectiveDuration");
        if (baseDuration.isZero() || baseDuration.isNegative()) {
            throw new IllegalArgumentException("baseDuration must be > 0");
        }
        if (effectiveDuration.isZero() || effectiveDuration.isNegative()) {
            throw new IllegalArgumentException("effectiveDuration must be > 0");
        }
        if (!Double.isFinite(multiplier) || multiplier <= 0.0D) {
            throw new IllegalArgumentException("multiplier must be finite and > 0");
        }
        routeTags = routeTags == null ? Set.of() : Set.copyOf(routeTags);
    }
}
