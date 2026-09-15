package kr.danta.core.combat;

import java.util.Objects;

/**
 * DEV-063 terrain hook.
 *
 * CombatSideInput already carries the authoritative terrainMultiplier used by
 * DEV-041+. This policy binds that multiplier to an explicit terrain category
 * without introducing unfixed terrain coefficients.
 */
public final class TerrainCombatPolicy {
    public double multiplier(BattlefieldContext battlefield, double configuredMultiplier) {
        Objects.requireNonNull(battlefield, "battlefield");
        if (!Double.isFinite(configuredMultiplier) || configuredMultiplier <= 0.0)
            throw new IllegalArgumentException("configuredMultiplier must be positive");
        return configuredMultiplier;
    }
}
