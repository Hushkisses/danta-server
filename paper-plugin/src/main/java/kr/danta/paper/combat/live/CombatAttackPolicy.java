package kr.danta.paper.combat.live;

import kr.danta.core.combat.TroopType;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * DEV-115 development-only live attack fixtures.
 *
 * These values exist only to make the Paper combat demo executable and are not final season balance.
 */
public final class CombatAttackPolicy {
    private final Map<TroopType, AttackSpec> specs;

    private CombatAttackPolicy(Map<TroopType, AttackSpec> specs) {
        this.specs = Map.copyOf(specs);
    }

    public static CombatAttackPolicy developmentDefaults() {
        EnumMap<TroopType, AttackSpec> specs = new EnumMap<>(TroopType.class);
        specs.put(TroopType.INFANTRY, new AttackSpec(AttackMode.MELEE, 2.8, 3.0, 0.24, 900L));
        specs.put(TroopType.SPEARMEN, new AttackSpec(AttackMode.MELEE, 3.2, 3.0, 0.22, 1000L));
        specs.put(TroopType.ARCHERS, new AttackSpec(AttackMode.RANGED, 14.0, 2.0, 0.20, 1400L));
        specs.put(TroopType.CAVALRY, new AttackSpec(AttackMode.MELEE, 3.0, 4.0, 0.32, 900L));
        specs.put(TroopType.MAGIC, new AttackSpec(AttackMode.SUPPORT_VISUAL, 9.0, 0.0, 0.20, 1200L));
        return new CombatAttackPolicy(specs);
    }

    public AttackSpec forType(TroopType troopType) {
        Objects.requireNonNull(troopType, "troopType");
        AttackSpec spec = specs.get(troopType);
        if (spec == null) throw new IllegalArgumentException("missing attack spec: " + troopType);
        return spec;
    }

    public double moveSpeed(TroopType troopType) {
        return forType(troopType).moveSpeed();
    }

    public enum AttackMode {
        MELEE,
        RANGED,
        SUPPORT_VISUAL
    }

    public record AttackSpec(
            AttackMode mode,
            double range,
            double damage,
            double moveSpeed,
            long cooldownMillis
    ) {
        public AttackSpec {
            Objects.requireNonNull(mode, "mode");
            if (!Double.isFinite(range) || range < 0.0) throw new IllegalArgumentException("invalid range");
            if (!Double.isFinite(damage) || damage < 0.0) throw new IllegalArgumentException("invalid damage");
            if (!Double.isFinite(moveSpeed) || moveSpeed < 0.0) throw new IllegalArgumentException("invalid moveSpeed");
            if (cooldownMillis < 0L) throw new IllegalArgumentException("invalid cooldownMillis");
        }
    }
}
