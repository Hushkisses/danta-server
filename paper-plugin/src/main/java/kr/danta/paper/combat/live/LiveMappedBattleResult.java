package kr.danta.paper.combat.live;

import kr.danta.core.combat.LogicalForceAiMappingPolicy;
import kr.danta.core.combat.TroopType;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/** DEV-119 immutable mapped-battle survivor snapshot. */
public final class LiveMappedBattleResult {
    private final LogicalForceAiMappingPolicy.Mapping redMapping;
    private final LogicalForceAiMappingPolicy.Mapping blueMapping;
    private final Map<TroopType, Integer> redSurvivingAi;
    private final Map<TroopType, Integer> blueSurvivingAi;

    private LiveMappedBattleResult(
            LogicalForceAiMappingPolicy.Mapping redMapping,
            LogicalForceAiMappingPolicy.Mapping blueMapping,
            Map<TroopType, Integer> redSurvivingAi,
            Map<TroopType, Integer> blueSurvivingAi
    ) {
        this.redMapping = Objects.requireNonNull(redMapping, "redMapping");
        this.blueMapping = Objects.requireNonNull(blueMapping, "blueMapping");
        this.redSurvivingAi = immutableCounts(redSurvivingAi);
        this.blueSurvivingAi = immutableCounts(blueSurvivingAi);
    }

    public static LiveMappedBattleResult capture(
            LogicalForceAiMappingPolicy.Mapping redMapping,
            LogicalForceAiMappingPolicy.Mapping blueMapping,
            Collection<LiveCombatUnit> survivingUnits
    ) {
        Objects.requireNonNull(survivingUnits, "survivingUnits");
        EnumMap<TroopType, Integer> red = zeroCounts();
        EnumMap<TroopType, Integer> blue = zeroCounts();

        for (LiveCombatUnit unit : survivingUnits) {
            Objects.requireNonNull(unit, "surviving unit");
            Map<TroopType, Integer> target = unit.side() == CombatSide.RED ? red : blue;
            target.put(unit.troopType(), Math.addExact(target.get(unit.troopType()), 1));
        }
        return new LiveMappedBattleResult(redMapping, blueMapping, red, blue);
    }

    public LogicalForceAiMappingPolicy.Mapping mapping(CombatSide side) {
        Objects.requireNonNull(side, "side");
        return side == CombatSide.RED ? redMapping : blueMapping;
    }

    public Map<TroopType, Integer> survivingAi(CombatSide side) {
        Objects.requireNonNull(side, "side");
        return side == CombatSide.RED ? redSurvivingAi : blueSurvivingAi;
    }

    private static EnumMap<TroopType, Integer> zeroCounts() {
        EnumMap<TroopType, Integer> result = new EnumMap<>(TroopType.class);
        for (TroopType type : TroopType.values()) result.put(type, 0);
        return result;
    }

    private static Map<TroopType, Integer> immutableCounts(Map<TroopType, Integer> source) {
        EnumMap<TroopType, Integer> copy = new EnumMap<>(TroopType.class);
        for (TroopType type : TroopType.values()) {
            int count = source.getOrDefault(type, 0);
            if (count < 0) throw new IllegalArgumentException("surviving AI count must be >= 0: " + type);
            copy.put(type, count);
        }
        return Collections.unmodifiableMap(copy);
    }
}
