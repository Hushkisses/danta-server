package kr.danta.core.siege;

/** DEV-110 logical siege lifecycle. Physical objective stages are introduced in DEV-113. */
public enum SiegePhase {
    CREATED,
    SCHEDULED,
    ACTIVE,
    RESOLVED,
    CANCELLED;

    public boolean terminal() { return this == RESOLVED || this == CANCELLED; }
}
