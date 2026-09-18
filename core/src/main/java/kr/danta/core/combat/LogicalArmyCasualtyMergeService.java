package kr.danta.core.combat;

import kr.danta.core.army.ArmyState;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/** DEV-119 validation-first bridge from live representative survival to authoritative logical army state. */
public final class LogicalArmyCasualtyMergeService {
    private final LogicalForceAiMappingPolicy mappingPolicy;

    public LogicalArmyCasualtyMergeService(LogicalForceAiMappingPolicy mappingPolicy) {
        this.mappingPolicy = Objects.requireNonNull(mappingPolicy, "mappingPolicy");
    }

    public LogicalArmyCasualtyMergeResult merge(
            ArmyState army,
            LogicalForceAiMappingPolicy.Mapping battleStartMapping,
            Map<TroopType, Integer> survivingAi
    ) {
        Objects.requireNonNull(army, "army");
        Objects.requireNonNull(battleStartMapping, "battleStartMapping");
        Objects.requireNonNull(survivingAi, "survivingAi");

        EnumMap<TroopType, Long> before = normalized(army.troopComposition());
        EnumMap<TroopType, Long> expected = normalized(battleStartMapping.logicalTroops());
        if (!before.equals(expected)) {
            throw new IllegalStateException("army troop composition changed after live battle mapping was created");
        }

        Map<TroopType, Long> projected = mappingPolicy.projectLogicalSurvivors(battleStartMapping, survivingAi);
        EnumMap<TroopType, Long> after = normalized(projected);
        EnumMap<TroopType, Long> losses = new EnumMap<>(TroopType.class);
        long totalLoss = 0L;

        for (TroopType type : TroopType.values()) {
            long loss = Math.subtractExact(before.get(type), after.get(type));
            if (loss < 0L) {
                throw new IllegalStateException("projected survivors exceed battle-start logical troops: " + type);
            }
            losses.put(type, loss);
            totalLoss = Math.addExact(totalLoss, loss);
        }

        army.replaceTroopComposition(after);
        return new LogicalArmyCasualtyMergeResult(before, after, losses, totalLoss);
    }

    private static EnumMap<TroopType, Long> normalized(Map<TroopType, Long> source) {
        EnumMap<TroopType, Long> result = new EnumMap<>(TroopType.class);
        for (TroopType type : TroopType.values()) {
            result.put(type, source.getOrDefault(type, 0L));
        }
        return result;
    }
}
