package kr.danta.core.combat;

import java.util.List;
import java.util.Objects;

/**
 * DEV-060 staged combat result.
 * The final power result remains compatible with the DEV-041 result shape.
 */
public record CombatV1Result(
        CombatResult finalResult,
        List<CombatPhaseResult> phases
) {
    public CombatV1Result {
        Objects.requireNonNull(finalResult, "finalResult");
        phases = List.copyOf(Objects.requireNonNull(phases, "phases"));
        if (phases.size() != CombatPhase.values().length) {
            throw new IllegalArgumentException("all combat phases must be present");
        }
        for (int i = 0; i < phases.size(); i++) {
            if (phases.get(i).phase() != CombatPhase.values()[i]) {
                throw new IllegalArgumentException("combat phases must follow the defined order");
            }
        }
    }
}
