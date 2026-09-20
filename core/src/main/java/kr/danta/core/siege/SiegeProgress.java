package kr.danta.core.siege;

import java.util.Objects;

/**
 * DEV-113 state machine for siege engagement cadence.
 * It intentionally models only ordered progression; combat resolution itself remains in the combat layer.
 */
public final class SiegeProgress {
    private final SiegeEngagementProfile profile;
    private SiegeStage stage;

    public SiegeProgress(SiegeEngagementProfile profile) {
        this(profile, initialStage(Objects.requireNonNull(profile, "profile")));
    }

    private SiegeProgress(SiegeEngagementProfile profile, SiegeStage stage) {
        this.profile = Objects.requireNonNull(profile, "profile");
        this.stage = Objects.requireNonNull(stage, "stage");
        validateStage(profile, stage);
    }

    public static SiegeProgress restored(SiegeEngagementProfile profile, SiegeStage stage) {
        return new SiegeProgress(profile, stage);
    }

    private static SiegeStage initialStage(SiegeEngagementProfile profile) {
        return switch (profile) {
            case NORMAL_QUICK -> SiegeStage.QUICK_RESOLUTION;
            case MAJOR_SINGLE_BATTLE -> SiegeStage.SINGLE_BATTLE;
            case CAPITAL_THREE_BATTLE -> SiegeStage.OUTER_BATTLE;
        };
    }

    private static void validateStage(SiegeEngagementProfile profile, SiegeStage stage) {
        boolean valid = switch (profile) {
            case NORMAL_QUICK -> stage == SiegeStage.QUICK_RESOLUTION || stage == SiegeStage.COMPLETE;
            case MAJOR_SINGLE_BATTLE -> stage == SiegeStage.SINGLE_BATTLE || stage == SiegeStage.COMPLETE;
            case CAPITAL_THREE_BATTLE -> stage == SiegeStage.OUTER_BATTLE
                    || stage == SiegeStage.OUTER_GATE
                    || stage == SiegeStage.PLAZA_BATTLE
                    || stage == SiegeStage.INNER_GATE
                    || stage == SiegeStage.CORE_BATTLE
                    || stage == SiegeStage.COMPLETE;
        };
        if (!valid) throw new IllegalArgumentException("stage " + stage + " is invalid for profile " + profile);
    }

    public SiegeEngagementProfile profile() {
        return profile;
    }

    public SiegeStage stage() {
        return stage;
    }

    public boolean complete() {
        return stage == SiegeStage.COMPLETE;
    }

    public void apply(SiegeProgressEvent event) {
        Objects.requireNonNull(event, "event");
        if (complete()) {
            throw new IllegalStateException("siege progression is already complete");
        }

        stage = switch (stage) {
            case QUICK_RESOLUTION -> require(event, SiegeProgressEvent.QUICK_RESOLVED, SiegeStage.COMPLETE);
            case SINGLE_BATTLE -> require(event, SiegeProgressEvent.BATTLE_WON, SiegeStage.COMPLETE);
            case OUTER_BATTLE -> require(event, SiegeProgressEvent.BATTLE_WON, SiegeStage.OUTER_GATE);
            case OUTER_GATE -> require(event, SiegeProgressEvent.GATE_BREACHED, SiegeStage.PLAZA_BATTLE);
            case PLAZA_BATTLE -> require(event, SiegeProgressEvent.BATTLE_WON, SiegeStage.INNER_GATE);
            case INNER_GATE -> require(event, SiegeProgressEvent.GATE_BREACHED, SiegeStage.CORE_BATTLE);
            case CORE_BATTLE -> require(event, SiegeProgressEvent.BATTLE_WON, SiegeStage.COMPLETE);
            case COMPLETE -> throw new IllegalStateException("siege progression is already complete");
        };
    }

    private SiegeStage require(SiegeProgressEvent actual, SiegeProgressEvent expected, SiegeStage next) {
        if (actual != expected) {
            throw new IllegalStateException("event " + actual + " is not valid during stage " + stage
                    + "; expected " + expected);
        }
        return next;
    }
}
