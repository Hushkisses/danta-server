package kr.danta.paper.combat.live;

import kr.danta.core.combat.TroopType;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/** DEV-115/116 deterministic development combat formation definition. */
public final class LiveCombatDemoFormation {
    private final List<Slot> slots;

    private LiveCombatDemoFormation(List<Slot> slots) {
        this.slots = List.copyOf(slots);
    }

    public static LiveCombatDemoFormation developmentDefaults() {
        ArrayList<Slot> slots = new ArrayList<>();
        addDefaultSide(slots, CombatSide.RED);
        addDefaultSide(slots, CombatSide.BLUE);
        return new LiveCombatDemoFormation(slots);
    }

    /** Creates an exact, side-balanced benchmark formation while cycling all troop types. */
    public static LiveCombatDemoFormation benchmark(int totalUnits) {
        if (totalUnits < 10 || totalUnits % 2 != 0) {
            throw new IllegalArgumentException("benchmark totalUnits must be even and >= 10");
        }
        ArrayList<Slot> slots = new ArrayList<>(totalUnits);
        addBenchmarkSide(slots, CombatSide.RED, totalUnits / 2);
        addBenchmarkSide(slots, CombatSide.BLUE, totalUnits / 2);
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

    private static void addDefaultSide(List<Slot> slots, CombatSide side) {
        int index = 0;
        for (TroopType troopType : TroopType.values()) {
            slots.add(new Slot(side, troopType, index++));
        }
    }

    private static void addBenchmarkSide(List<Slot> slots, CombatSide side, int sideUnits) {
        TroopType[] types = TroopType.values();
        for (int index = 0; index < sideUnits; index++) {
            slots.add(new Slot(side, types[index % types.length], index));
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
