package kr.danta.core.general;

import kr.danta.core.combat.TroopType;

import java.util.Objects;
import java.util.Optional;

/**
 * DEV-072 general-to-troop synergy contract.
 *
 * Design v0.3 requires generals to affect specific troop types or the whole army,
 * but does not fix an authoritative bonus percentage or assignment table.
 */
public record GeneralTroopSynergy(
        Scope scope,
        TroopType troopType
) {
    public enum Scope {
        ALL_TROOPS,
        TROOP_TYPE
    }

    public GeneralTroopSynergy {
        Objects.requireNonNull(scope, "scope");
        if (scope == Scope.TROOP_TYPE && troopType == null)
            throw new IllegalArgumentException("troopType is required for TROOP_TYPE synergy");
        if (scope == Scope.ALL_TROOPS && troopType != null)
            throw new IllegalArgumentException("troopType must be null for ALL_TROOPS synergy");
    }

    public static GeneralTroopSynergy allTroops() {
        return new GeneralTroopSynergy(Scope.ALL_TROOPS, null);
    }

    public static GeneralTroopSynergy troopType(TroopType troopType) {
        return new GeneralTroopSynergy(Scope.TROOP_TYPE, Objects.requireNonNull(troopType, "troopType"));
    }

    public boolean appliesTo(TroopType candidate) {
        Objects.requireNonNull(candidate, "candidate");
        return scope == Scope.ALL_TROOPS || troopType == candidate;
    }

    public Optional<TroopType> targetedTroopType() {
        return Optional.ofNullable(troopType);
    }
}
