package kr.danta.paper.combat.live;

import kr.danta.core.combat.LogicalForceAiMappingPolicy;
import kr.danta.core.combat.TroopType;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/** DEV-115/116/117 deterministic development combat formation definition. */
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

    public static LiveCombatDemoFormation fromLogicalForces(
            Map<TroopType, Long> redLogicalForce,
            Map<TroopType, Long> blueLogicalForce,
            LogicalForceAiMappingPolicy mappingPolicy
    ) {
        if (redLogicalForce == null) throw new NullPointerException("redLogicalForce");
        if (blueLogicalForce == null) throw new NullPointerException("blueLogicalForce");
        if (mappingPolicy == null) throw new NullPointerException("mappingPolicy");

        return fromMappings(mappingPolicy.map(redLogicalForce), mappingPolicy.map(blueLogicalForce));
    }

    public static LiveCombatDemoFormation fromMappings(
            LogicalForceAiMappingPolicy.Mapping red,
            LogicalForceAiMappingPolicy.Mapping blue
    ) {
        if (red == null) throw new NullPointerException("red");
        if (blue == null) throw new NullPointerException("blue");
        ArrayList<Slot> slots = new ArrayList<>(red.totalAiUnits() + blue.totalAiUnits());
        addMappedSide(slots, CombatSide.RED, red);
        addMappedSide(slots, CombatSide.BLUE, blue);
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

    public int count(CombatSide side, TroopType troopType) {
        int count = 0;
        for (Slot slot : slots) {
            if (slot.side() == side && slot.troopType() == troopType) count++;
        }
        return count;
    }

    public int sideUnitCount(CombatSide side) {
        int count = 0;
        for (Slot slot : slots) {
            if (slot.side() == side) count++;
        }
        return count;
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

    private static void addMappedSide(
            List<Slot> slots,
            CombatSide side,
            LogicalForceAiMappingPolicy.Mapping mapping
    ) {
        int index = 0;
        for (TroopType troopType : TroopType.values()) {
            int representatives = mapping.aiUnits(troopType);
            for (int i = 0; i < representatives; i++) {
                slots.add(new Slot(side, troopType, index++));
            }
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
