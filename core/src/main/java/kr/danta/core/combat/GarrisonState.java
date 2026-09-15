package kr.danta.core.combat;

import java.util.Objects;
import java.util.Optional;

/** Execution-plan DEV-043 minimum strategic-point garrison aggregate. */
public final class GarrisonState {
    private final String pointId;
    private GarrisonController controller;
    private String nationId;
    private int troopCount;

    public GarrisonState(String pointId, GarrisonController controller, String nationId, int troopCount) {
        this.pointId = requireId(pointId, "pointId");
        setController(controller, nationId);
        setTroopCount(troopCount);
    }

    public static GarrisonState npc(String pointId, int troopCount) {
        return new GarrisonState(pointId, GarrisonController.NPC, null, troopCount);
    }

    public String pointId() { return pointId; }
    public synchronized GarrisonController controller() { return controller; }
    public synchronized Optional<String> nationId() { return Optional.ofNullable(nationId); }
    public synchronized int troopCount() { return troopCount; }

    public synchronized void setTroopCount(int troopCount) {
        if (troopCount < 0) throw new IllegalArgumentException("troopCount must be >= 0");
        this.troopCount = troopCount;
    }

    public synchronized void setController(GarrisonController controller, String nationId) {
        this.controller = Objects.requireNonNull(controller, "controller");
        if (controller == GarrisonController.NPC) {
            this.nationId = null;
        } else {
            this.nationId = requireId(nationId, "nationId");
        }
    }

    private static String requireId(String value, String label) {
        Objects.requireNonNull(value, label);
        String normalized = value.trim();
        if (!normalized.matches("[A-Za-z0-9_-]{1,48}"))
            throw new IllegalArgumentException(label + " must match [A-Za-z0-9_-]{1,48}");
        return normalized;
    }
}
