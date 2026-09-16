package kr.danta.paper.combat.live;

/** DEV-115 shared-loop cadence for frequent movement and slower CombatAI reevaluation. */
public final class LiveCombatLoopCadence {
    private final int movementIntervalTicks;
    private final int decisionIntervalTicks;
    private long tick;

    public LiveCombatLoopCadence(int movementIntervalTicks, int decisionIntervalTicks) {
        if (movementIntervalTicks <= 0) throw new IllegalArgumentException("movementIntervalTicks must be positive");
        if (decisionIntervalTicks <= 0) throw new IllegalArgumentException("decisionIntervalTicks must be positive");
        this.movementIntervalTicks = movementIntervalTicks;
        this.decisionIntervalTicks = decisionIntervalTicks;
    }

    public TickSchedule advance() {
        tick++;
        return new TickSchedule(
                tick % movementIntervalTicks == 0,
                tick % decisionIntervalTicks == 0
        );
    }

    public record TickSchedule(boolean movementDue, boolean decisionDue) {}
}
