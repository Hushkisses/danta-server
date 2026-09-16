package kr.danta.core.siege;

import java.util.Objects;

/**
 * DEV-112 objective anchor. DEV-113 will attach gate/core destruction rules.
 */
public record SiegeObjective(String objectiveId, SiegeObjectiveType type, int x, int y, int z) {
    public SiegeObjective {
        Objects.requireNonNull(objectiveId, "objectiveId");
        Objects.requireNonNull(type, "type");
        if (objectiveId.isBlank()) throw new IllegalArgumentException("objectiveId must not be blank");
    }
}
