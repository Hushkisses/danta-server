package kr.danta.paper.combat.live;

import kr.danta.core.combat.TroopType;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/** DEV-115 deterministic development demo formation definition. */
public final class LiveCombatDemoFormation {
    private final List<Slot> slots;

    private LiveCombatDemoFormation(List<Slot> slots) {
        this.slots = List.copyOf(slots);
    }

    public static LiveCombatDemoFormation developmentDefaults() {
        ArrayList<Slot> slots = new ArrayList<>();
        addSide(slots, CombatSide.RED);
        addSide(slots, CombatSide.BLUE);
        return new LiveCombatDemoFormation(slots);
    }

    public List<Slot> slots() {
        return slots;
    }

    public EnumSet<TroopType> troopTypesFor(CombatSide side) {
        EnumSet<TroopType> result = EnumSet.noneOf(TroopType.class);
        for (Slot slot : slots) {
            if (slot.side() == side) result.add(slot.troopType());
        }
        return result;
    }

    private static void addSide(List<Slot> slots, CombatSide side) {
        int index = 0;
        for (TroopType troopType : TroopType.values()) {
            slots.add(new Slot(side, troopType, index++));
        }
    }

    public record Slot(CombatSide side, TroopType troopType, int formationIndex) {
        public Slot {
            if (side == null) throw new NullPointerException("side");
            if (troopType == null) throw new NullPointerException("troopType");
            if (formationIndex < 0) throw new IllegalArgumentException("formationIndex < 0");
        }
    }
}
