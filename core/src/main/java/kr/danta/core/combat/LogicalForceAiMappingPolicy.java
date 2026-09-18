package kr.danta.core.combat;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * DEV-117 bridge between authoritative logical troop counts and a bounded number of live AI representatives.
 *
 * <p>The ratio is intentionally configurable. The development default is temporary and must not be treated
 * as final season balance.</p>
 */
public final class LogicalForceAiMappingPolicy {
    public static final int DEVELOPMENT_LOGICAL_TROOPS_PER_AI = 100;

    private final int logicalTroopsPerAi;

    public LogicalForceAiMappingPolicy(int logicalTroopsPerAi) {
        if (logicalTroopsPerAi <= 0) {
            throw new IllegalArgumentException("logicalTroopsPerAi must be > 0");
        }
        this.logicalTroopsPerAi = logicalTroopsPerAi;
    }

    public static LogicalForceAiMappingPolicy developmentDefaults() {
        return new LogicalForceAiMappingPolicy(DEVELOPMENT_LOGICAL_TROOPS_PER_AI);
    }

    public int logicalTroopsPerAi() {
        return logicalTroopsPerAi;
    }

    public Mapping map(Map<TroopType, Long> logicalForce) {
        Objects.requireNonNull(logicalForce, "logicalForce");
        EnumMap<TroopType, Long> logical = new EnumMap<>(TroopType.class);
        EnumMap<TroopType, Integer> ai = new EnumMap<>(TroopType.class);

        for (TroopType type : TroopType.values()) {
            long count = logicalForce.getOrDefault(type, 0L);
            if (count < 0L) {
                throw new IllegalArgumentException("logical troop count must be >= 0: " + type);
            }
            logical.put(type, count);
            ai.put(type, representativesFor(count));
        }
        return new Mapping(logical, ai, logicalTroopsPerAi);
    }

    public Map<TroopType, Long> projectLogicalSurvivors(
            Mapping mapping,
            Map<TroopType, Integer> survivingAi
    ) {
        Objects.requireNonNull(mapping, "mapping");
        Objects.requireNonNull(survivingAi, "survivingAi");

        EnumMap<TroopType, Long> result = new EnumMap<>(TroopType.class);
        for (TroopType type : TroopType.values()) {
            int spawned = mapping.aiUnits(type);
            int survivors = survivingAi.getOrDefault(type, 0);
            if (survivors < 0 || survivors > spawned) {
                throw new IllegalArgumentException(
                        "surviving AI count out of range for " + type + ": " + survivors + "/" + spawned);
            }

            long logical = mapping.logicalTroops(type);
            if (spawned == 0 || survivors == 0 || logical == 0L) {
                result.put(type, 0L);
            } else if (survivors == spawned) {
                result.put(type, logical);
            } else {
                long projected = Math.round((double) logical * survivors / spawned);
                result.put(type, Math.max(0L, Math.min(logical, projected)));
            }
        }
        return Collections.unmodifiableMap(result);
    }

    private int representativesFor(long logicalTroops) {
        if (logicalTroops == 0L) return 0;
        long representatives = 1L + (logicalTroops - 1L) / logicalTroopsPerAi;
        if (representatives > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("logical force is too large to map to live AI");
        }
        return (int) representatives;
    }

    public static final class Mapping {
        private final Map<TroopType, Long> logicalTroops;
        private final Map<TroopType, Integer> aiUnits;
        private final int logicalTroopsPerAi;

        private Mapping(
                Map<TroopType, Long> logicalTroops,
                Map<TroopType, Integer> aiUnits,
                int logicalTroopsPerAi
        ) {
            this.logicalTroops = Collections.unmodifiableMap(new EnumMap<>(logicalTroops));
            this.aiUnits = Collections.unmodifiableMap(new EnumMap<>(aiUnits));
            this.logicalTroopsPerAi = logicalTroopsPerAi;
        }

        public long logicalTroops(TroopType type) {
            Objects.requireNonNull(type, "type");
            return logicalTroops.getOrDefault(type, 0L);
        }

        public int aiUnits(TroopType type) {
            Objects.requireNonNull(type, "type");
            return aiUnits.getOrDefault(type, 0);
        }

        public int totalAiUnits() {
            int total = 0;
            for (int count : aiUnits.values()) {
                total = Math.addExact(total, count);
            }
            return total;
        }

        public int logicalTroopsPerAi() {
            return logicalTroopsPerAi;
        }

        public Map<TroopType, Long> logicalTroops() {
            return logicalTroops;
        }

        public Map<TroopType, Integer> aiUnits() {
            return aiUnits;
        }
    }
}
