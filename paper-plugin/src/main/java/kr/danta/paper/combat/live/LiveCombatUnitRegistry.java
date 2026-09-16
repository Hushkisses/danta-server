package kr.danta.paper.combat.live;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Tracks DEV-115 live combat units without making the registry strategic state. */
public final class LiveCombatUnitRegistry {
    private final Map<UUID, LiveCombatUnit> byUnitId = new LinkedHashMap<>();
    private final Map<UUID, LiveCombatUnit> byPrimaryEntityId = new LinkedHashMap<>();
    private final Map<UUID, LiveCombatUnit> byEntityId = new LinkedHashMap<>();

    public void register(LiveCombatUnit unit) {
        if (unit == null) throw new NullPointerException("unit");

        LiveCombatUnit previous = byUnitId.put(unit.unitId(), unit);
        if (previous != null) {
            removeEntityIndexes(previous);
        }
        byPrimaryEntityId.put(unit.primaryEntityId(), unit);
        byEntityId.put(unit.primaryEntityId(), unit);
        if (unit.mountEntityId() != null) {
            byEntityId.put(unit.mountEntityId(), unit);
        }
    }

    public Optional<LiveCombatUnit> byPrimaryEntity(UUID entityId) {
        if (entityId == null) return Optional.empty();
        return Optional.ofNullable(byPrimaryEntityId.get(entityId));
    }

    public Optional<LiveCombatUnit> byEntity(UUID entityId) {
        if (entityId == null) return Optional.empty();
        return Optional.ofNullable(byEntityId.get(entityId));
    }

    public Collection<LiveCombatUnit> units() {
        return java.util.List.copyOf(new ArrayList<>(byUnitId.values()));
    }

    public void remove(UUID unitId) {
        if (unitId == null) return;
        LiveCombatUnit removed = byUnitId.remove(unitId);
        if (removed != null) {
            removeEntityIndexes(removed);
        }
    }

    public void clear() {
        byUnitId.clear();
        byPrimaryEntityId.clear();
        byEntityId.clear();
    }

    private void removeEntityIndexes(LiveCombatUnit unit) {
        byPrimaryEntityId.remove(unit.primaryEntityId());
        byEntityId.remove(unit.primaryEntityId());
        if (unit.mountEntityId() != null) {
            byEntityId.remove(unit.mountEntityId());
        }
    }
}
