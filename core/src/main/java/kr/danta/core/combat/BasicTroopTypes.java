package kr.danta.core.combat;

import java.util.List;
import java.util.Map;

public final class BasicTroopTypes {
    public static final double SOFT_COUNTER_MULTIPLIER = 1.25;

    private static final Map<TroopType, TroopTypeDefinition> DEFINITIONS = Map.of(
            TroopType.INFANTRY, new TroopTypeDefinition(TroopType.INFANTRY, TroopRole.FRONTLINE, 1.0,
                    TroopType.SPEARMEN, SOFT_COUNTER_MULTIPLIER),
            TroopType.SPEARMEN, new TroopTypeDefinition(TroopType.SPEARMEN, TroopRole.FRONTLINE, 1.0,
                    TroopType.CAVALRY, SOFT_COUNTER_MULTIPLIER),
            TroopType.ARCHERS, new TroopTypeDefinition(TroopType.ARCHERS, TroopRole.RANGED, 1.0,
                    TroopType.INFANTRY, SOFT_COUNTER_MULTIPLIER),
            TroopType.CAVALRY, new TroopTypeDefinition(TroopType.CAVALRY, TroopRole.MOBILE, 1.0,
                    TroopType.ARCHERS, SOFT_COUNTER_MULTIPLIER)
    );

    private BasicTroopTypes() {}

    public static TroopTypeDefinition definition(TroopType type) {
        TroopTypeDefinition definition = DEFINITIONS.get(type);
        if (definition == null) throw new IllegalArgumentException("unsupported troop type: " + type);
        return definition;
    }

    public static List<TroopTypeDefinition> all() {
        return List.of(
                definition(TroopType.INFANTRY),
                definition(TroopType.SPEARMEN),
                definition(TroopType.ARCHERS),
                definition(TroopType.CAVALRY)
        );
    }
}
