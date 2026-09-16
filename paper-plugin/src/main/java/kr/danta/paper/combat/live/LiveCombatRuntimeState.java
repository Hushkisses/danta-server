package kr.danta.paper.combat.live;

/** Minimal DEV-115 lifecycle state for the live combat demo. */
public final class LiveCombatRuntimeState {
    private boolean active;

    public boolean begin() {
        if (active) return false;
        active = true;
        return true;
    }

    public void finish() {
        active = false;
    }

    public boolean active() {
        return active;
    }
}
