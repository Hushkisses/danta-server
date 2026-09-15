package kr.danta.core.army;

import java.util.Objects;
import java.util.Optional;

public record ArmyAdvanceDecision(boolean shouldStop, ArmyAdvanceStopReason reason, String detail) {
    public ArmyAdvanceDecision {
        if (shouldStop) Objects.requireNonNull(reason, "reason");
        if (!shouldStop && reason != null) throw new IllegalArgumentException("continue decision cannot have a stop reason");
        detail = detail == null ? "" : detail;
    }

    public static ArmyAdvanceDecision continueAdvance() {
        return new ArmyAdvanceDecision(false, null, "");
    }

    public static ArmyAdvanceDecision stop(ArmyAdvanceStopReason reason, String detail) {
        return new ArmyAdvanceDecision(true, reason, detail);
    }

    public Optional<ArmyAdvanceStopReason> stopReason() {
        return Optional.ofNullable(reason);
    }
}
