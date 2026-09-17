package kr.danta.paper.combat.live;

import kr.danta.core.combat.TroopType;

import java.util.Collection;
import java.util.Objects;

/** Determines whether a magic unit still has a real allied combat group to support. */
public final class CombatAlliedSupportPolicy {
    private CombatAlliedSupportPolicy() {}

    public static boolean hasCombatGroup(LiveCombatUnit self, Collection<LiveCombatUnit> units) {
        Objects.requireNonNull(self, "self");
        if (units == null || units.isEmpty()) return false;

        return units.stream()
                .filter(Objects::nonNull)
                .anyMatch(candidate -> candidate.side() == self.side()
                        && !candidate.unitId().equals(self.unitId())
                        && candidate.troopType() != TroopType.MAGIC);
    }
}
